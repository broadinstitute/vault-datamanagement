package org.broadinstitute.dsde.vault.datamanagement.domain

import java.sql.Timestamp
import java.util.UUID

case class Entity
(
  entityType: String,
  createdBy: String,
  createdDate: Option[Timestamp] = None,
  modifiedBy: Option[String] = None,
  modifiedDate: Option[Timestamp] = None,
  guid: Option[String] = None
  )

trait EntityComponent {
  this: DriverComponent =>

  import driver.simple._

  class Entities(tag: Tag) extends Table[Entity](tag, "ENTITY") {
    def entityType = column[String]("ENTITY_TYPE")

    def createdBy = column[String]("CREATED_BY")

    def createdDate = column[Timestamp]("CREATED_DATE")

    def modifiedBy = column[String]("MODIFIED_BY")

    def modifiedDate = column[Timestamp]("MODIFIED_DATE")

    def guid = column[String]("GUID", O.PrimaryKey)

    override def * = (entityType, createdBy, createdDate.?, modifiedBy.?, modifiedDate.?, guid.?) <>(Entity.tupled, Entity.unapply)
  }

  val entities = TableQuery[Entities]

  val entitiesCompiled = Compiled(entities)

  private val entitiesByGUID = Compiled(
    (guid: Column[String]) => for {
      entity <- entities
      if entity.guid === guid
    } yield entity)

  private val entitiesByGUIDModified = Compiled(
    (guid: Column[String]) => for {
      entity <- entities
      if entity.guid === guid
    } yield (entity.modifiedBy, entity.modifiedDate))

  def insertEntity(entityType: String, createdBy: String)(implicit session: Session): Entity = {
    val entity = Entity(entityType, createdBy,
      Option(new Timestamp(System.currentTimeMillis())),
      guid = Option(UUID.randomUUID.toString))
    entitiesCompiled += entity
    entity
  }

  def updateEntity(guid: String, updatedBy: String)(implicit session: Session) {
    entitiesByGUIDModified(guid).update(updatedBy, new Timestamp(System.currentTimeMillis()))
  }

  def getEntity(guid: String)(implicit session: Session): Option[Entity] = {
    entitiesByGUID(guid).firstOption
  }
}
