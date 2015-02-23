package org.broadinstitute.dsde.vault.datamanagement.controller

import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.DatabaseConfig
import org.broadinstitute.dsde.vault.datamanagement.domain.{Attribute, DataAccess}

import scala.slick.jdbc.JdbcBackend._

object DataManagementController {
  val dataAccess = new DataAccess(DatabaseConfig.slickDriver)

  // TODO: Connection pooling
  def database: Database = Database.forURL(DatabaseConfig.jdbcUrl,
    DatabaseConfig.jdbcUser, DatabaseConfig.jdbcPassword,
    null, DatabaseConfig.jdbcDriver)

  def createAttribute(name: String, value: String): Attribute = {
    database withTransaction {
      implicit session =>
        dataAccess.insert(Attribute(name, value))
    }
  }

  def getAttribute(id: Int): Attribute = {
    database withTransaction {
      implicit session =>
        dataAccess.getAttribute(id)
    }
  }
}
