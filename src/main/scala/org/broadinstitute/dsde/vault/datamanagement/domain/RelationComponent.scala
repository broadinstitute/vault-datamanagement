package org.broadinstitute.dsde.vault.datamanagement.domain

case class Relation
(
  relationGUID: String,
  entity1GUID: String,
  entity2GUID: String
  )

trait RelationComponent {
  this: DriverComponent with EntityComponent =>

  import driver.simple._

  class Relations(tag: Tag) extends Table[Relation](tag, "ENTITY_RELATION") {
    def relationGUID = column[String]("RELATION_ENTITY_GUID", O.PrimaryKey)

    def entity1GUID = column[String]("ENTITY_GUID_1")

    def entity2GUID = column[String]("ENTITY_GUID_2")

    override def * = (relationGUID, entity1GUID, entity2GUID) <>(Relation.tupled, Relation.unapply)

    def relation = foreignKey("FK_ENTITY_RELATION_ENTITY", relationGUID, entities)(_.guid)

    def entity1 = foreignKey("FK_ENTITY_RELATION_ENTITY_1", entity1GUID, entities)(_.guid)

    def entity2 = foreignKey("FK_ENTITY_RELATION_ENTITY_2", entity2GUID, entities)(_.guid)
  }

  val relations = TableQuery[Relations]

  private val relationsCompiled = Compiled(relations)

  def insertRelation(relationGUID: String, entity1GUID: String, entity2GUID: String)
                    (implicit session: Session): Relation = {
    val relation = new Relation(relationGUID, entity1GUID, entity2GUID)
    relationsCompiled += relation
    relation
  }
}
