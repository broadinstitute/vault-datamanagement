package org.broadinstitute.dsde.vault.datamanagement.domain

case class Attribute
(
  entityGUID: String,
  name: String,
  value: String,
  id: Option[Int] = None
  )

trait AttributeComponent {
  this: DriverComponent with EntityComponent =>

  import driver.simple._

  class Attributes(tag: Tag) extends Table[Attribute](tag, "ATTRIBUTE") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def entityGUID = column[String]("ENTITY_GUID")

    def name = column[String]("NAME")

    def value = column[String]("ATTRVALUE")

    override def * = (entityGUID, name, value, id.?) <>(Attribute.tupled, Attribute.unapply)

    def entity = foreignKey("FK_ATTRIBUTE_ENTITY", entityGUID, entities)(_.guid)
  }

  val attributes = TableQuery[Attributes]

  val attributesCompiled = Compiled(attributes)

  private val attributesAutoInc = attributesCompiled returning attributes.map(_.id) into {
    case (a, id) => a.copy(id = Some(id))
  }

  private val attributeByID = Compiled(
    (id: Column[Int]) => for {
      attribute <- attributes
      if attribute.id === id
    } yield attribute)

  def insertAttribute(entityGUID: String, name: String, value: String)(implicit session: Session): Attribute = {
    attributesAutoInc.insert(Attribute(entityGUID, name, value))
  }

  def getAttribute(id: Int)(implicit session: Session): Attribute = {
    attributeByID(id).first
  }
}
