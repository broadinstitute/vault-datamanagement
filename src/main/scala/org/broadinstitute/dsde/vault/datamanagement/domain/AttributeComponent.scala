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

import com.wordnik.swagger.annotations.ApiModelProperty

import scala.annotation.meta.field

case class Attribute
(
  @(ApiModelProperty@field)(value = "owning entity GUID", required = true)
  entityGUID: String,

  @(ApiModelProperty@field)(value = "attribute name", required = true)
  name: String,

  @(ApiModelProperty@field)(value = "attribute value", required = true)
  value: String,

  @(ApiModelProperty@field)(value = "attribute id", required = false, dataType = "Int")
  id: Option[Int] = None
  )

trait AttributeComponent {
  this: DriverComponent =>

  import driver.simple._

  class Attributes(tag: Tag) extends Table[Attribute](tag, "ATTRIBUTE") {
    def entityGUID = column[String]("ENTITY_GUID")

    def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def value = column[String]("ATTRVALUE")

    override def * = (entityGUID, name, value, id) <>(Attribute.tupled, Attribute.unapply)
  }

  val attributes = TableQuery[Attributes]

  private val attributesAutoInc = attributes returning attributes.map(_.id) into {
    case (a, id) => a.copy(id = id)
  }

  def insert(attribute: Attribute)(implicit session: Session): Attribute =
    attributesAutoInc.insert(attribute)

  def getAttribute(id: Int)(implicit session: Session): Attribute = {
    attributes.filter(_.id === id).first
  }
}
