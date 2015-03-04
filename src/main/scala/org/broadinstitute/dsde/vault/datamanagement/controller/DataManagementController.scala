package org.broadinstitute.dsde.vault.datamanagement.controller

import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.DatabaseConfig
import org.broadinstitute.dsde.vault.datamanagement.domain.{Attribute, Entity, DataAccess}

import scala.slick.jdbc.JdbcBackend._

object DataManagementController {
  val dataAccess = new DataAccess(DatabaseConfig.slickDriver)

  // TODO: Connection pooling
  def database: Database = Database.forURL(DatabaseConfig.jdbcUrl,
    DatabaseConfig.jdbcUser, DatabaseConfig.jdbcPassword,
    null, DatabaseConfig.jdbcDriver)

  def createEntity(entityType: String, createdBy: String): Entity = {
    database withTransaction {
      implicit session =>
        dataAccess.insert(Entity(entityType,createdBy))
    }
  }

  def createAttribute(entityGUID: String, name: String, value: String): Attribute = {
    database withTransaction {
      implicit session =>
        dataAccess.insert(Attribute(entityGUID, name, value))
    }
  }

  def getAttribute(id: Int): Attribute = {
    database withTransaction {
      implicit session =>
        dataAccess.getAttribute(id)
    }
  }
}
