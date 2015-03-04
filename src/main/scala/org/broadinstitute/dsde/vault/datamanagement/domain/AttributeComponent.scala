package org.broadinstitute.dsde.vault.datamanagement.domain

import com.wordnik.swagger.annotations.ApiModelProperty

import scala.annotation.meta.field

case class Attribute
(
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

  class Attributes(tag: Tag) extends Table[Attribute](tag, "attribute") {
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def value = column[String]("attrvalue")

    override def * = (name, value, id) <>(Attribute.tupled, Attribute.unapply)
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
