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

case class Relation (
    relationGUID: String,
    entity1GUID: String,
    entity2GUID: String
)

trait RelationComponent {
  this: DriverComponent =>

  import driver.simple._

  class Relations(tag: Tag) extends Table[Relation](tag, "ENTITY_RELATION") {
    def relationGUID = column[String]("RELATION_ENTITY_GUID", O.PrimaryKey)

    def entity1GUID = column[String]("ENTITY_GUID_1")

    def entity2GUID = column[String]("ENTITY_GUID_2")

    override def * = (relationGUID,entity1GUID,entity2GUID) <>(Relation.tupled, Relation.unapply)
  }

  val relations = TableQuery[Relations]

  def getRelation(guid: String)(implicit session: Session): Relation = {
    relations.filter(_.relationGUID === guid).first
  }
}
