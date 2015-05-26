package org.broadinstitute.dsde.vault.datamanagement.domain

import java.sql.Timestamp
import java.util.UUID

import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig
import org.broadinstitute.dsde.vault.datamanagement.controller.EntityType

case class Entity
(
  entityType: String,
  createdBy: String,
  createdDate: Option[Timestamp] = None,
  modifiedBy: Option[String] = None,
  modifiedDate: Option[Timestamp] = None,
  guid: Option[String] = None,
  bossID: Option[String] = None
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

    def bossID = column[String]("BOSS_ID")

    override def * = (entityType, createdBy, createdDate.?, modifiedBy.?, modifiedDate.?, guid.?, bossID.?) <>(Entity.tupled, Entity.unapply)
  }

  val entities = TableQuery[Entities]

  val entitiesCompiled = Compiled(entities)

  val entitiesPageLimitDefault = DataManagementConfig.DatabaseConfig.entitiesPageLimitDefault

  // http://slick.typesafe.com/doc/2.1.0/queries.html#compiled-queries
  val entitiesPageLimitCompiled = Compiled((pageLimit: ConstColumn[Long]) => entities.take(pageLimit))

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

  def getEntityList(pageLimit: Option[Int])(implicit session: Session): List[Entity] = {
    if (pageLimit.isDefined || entitiesPageLimitDefault.isDefined) {
      val limit = pageLimit.getOrElse(entitiesPageLimitDefault.get)
      entitiesPageLimitCompiled(limit).list
    } else {
      entities.list
    }
  }

  def getCollectionList()(implicit session: Session): List[Entity] = {
    entities.filter(_.entityType===EntityType.UBAM_COLLECTION.databaseKey).list
  }

  def getEntityCollectionListByIdList(ids:List[String])(implicit session: Session): List[Entity] = {
    entities.filter(_.guid inSet  ids).filter(_.entityType===EntityType.UBAM_COLLECTION.databaseKey).list
  }
}
