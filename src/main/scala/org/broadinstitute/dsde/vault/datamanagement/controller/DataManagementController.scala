package org.broadinstitute.dsde.vault.datamanagement.controller

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.DatabaseConfig
import org.broadinstitute.dsde.vault.datamanagement.domain._
import org.broadinstitute.dsde.vault.datamanagement.model.UnmappedBAM

import scala.slick.jdbc.JdbcBackend._

object DataManagementController {
  val dataAccess = new DataAccess(DatabaseConfig.slickDriver)

  def createUnmappedBAM(unmappedBAM: UnmappedBAM, createdBy: String): UnmappedBAM = {
    database withTransaction {
      implicit session =>
        val entity = dataAccess.insertEntity("unmappedBAM", createdBy)
        dataAccess.addFiles(entity.guid.get, createdBy, unmappedBAM.files)
        dataAccess.addMetadata(entity.guid.get, unmappedBAM.metadata)
        unmappedBAM.copy(id = entity.guid)
    }
  }

  private val cpds = new ComboPooledDataSource
  cpds.setDriverClass(DatabaseConfig.jdbcDriver)
  cpds.setJdbcUrl(DatabaseConfig.jdbcUrl)
  cpds.setUser(DatabaseConfig.jdbcUser)
  cpds.setPassword(DatabaseConfig.jdbcPassword)
  DatabaseConfig.c3p0MaxStatementsOption.map(cpds.setMaxStatements)

  def database: Database = Database.forDataSource(cpds)

  def getUnmappedBAM(id: String): Option[UnmappedBAM] = {
    database withTransaction {
      implicit session =>
        dataAccess.getEntity(id).map(
          entity => {
            val files = dataAccess.getFiles(entity.guid.get)
            val metadata = dataAccess.getMetadata(entity.guid.get)
            UnmappedBAM(files, metadata, entity.guid)
          }
        )
    }
  }
}
