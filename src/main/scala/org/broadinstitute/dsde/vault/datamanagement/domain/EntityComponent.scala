/**
 * Copyright 2015 Broad Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadinstitute.dsde.vault.datamanagement.domain

import java.sql.Timestamp
import java.util.UUID

case class Entity (
    entityType: String,
    createdBy: String,
    createdDate: Timestamp = new Timestamp(System.currentTimeMillis),
    modifiedBy: Option[String] = None,
    modifiedDate: Option[Timestamp] = None,
    guid: String = UUID.randomUUID.toString
)

trait EntityComponent {
  this: DriverComponent =>

  import driver.simple._

  class Entities(tag: Tag) extends Table[Entity](tag, "ENTITY") {
    def entityType = column[String]("ENTITY_TYPE")

    def createdBy = column[String]("CREATED_BY")

    def createdDate = column[Timestamp]("CREATED_DATE")

    def modifiedBy = column[Option[String]]("MODIFIED_BY")

    def modifiedDate = column[Option[Timestamp]]("MODIFIED_DATE")

    def guid = column[String]("GUID", O.PrimaryKey)

    override def * = (entityType,createdBy,createdDate,modifiedBy,modifiedDate,guid) <>(Entity.tupled, Entity.unapply)
  }

  val entities = TableQuery[Entities]

  def insert(entity: Entity)(implicit session: Session): Entity = {
    entities += entity
    entity.copy()
  }

  def getEntity(guid: String)(implicit session: Session): Option[Entity] = {
    entities.filter(_.guid === guid).firstOption
  }
}
