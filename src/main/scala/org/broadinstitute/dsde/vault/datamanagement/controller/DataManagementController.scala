package org.broadinstitute.dsde.vault.datamanagement.controller

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.DatabaseConfig
import org.broadinstitute.dsde.vault.datamanagement.domain._
import org.broadinstitute.dsde.vault.datamanagement.model.UnmappedBAM

import scala.slick.jdbc.JdbcBackend._

object DataManagementController {
  val dataAccess = new DataAccess(DatabaseConfig.slickDriver)

  private val comboPooledDataSource = new ComboPooledDataSource
  comboPooledDataSource.setDriverClass(DatabaseConfig.jdbcDriver)
  comboPooledDataSource.setJdbcUrl(DatabaseConfig.jdbcUrl)
  comboPooledDataSource.setUser(DatabaseConfig.jdbcUser)
  comboPooledDataSource.setPassword(DatabaseConfig.jdbcPassword)
  DatabaseConfig.c3p0MaxStatementsOption.map(comboPooledDataSource.setMaxStatements)

  def database: Database = Database.forDataSource(comboPooledDataSource)

  def createUnmappedBAM(unmappedBAM: UnmappedBAM, createdBy: String): UnmappedBAM = {
    database withTransaction {
      implicit session =>
        val entity = dataAccess.insertEntity(EntityType.UNMAPPED_BAM.databaseKey, createdBy)
        dataAccess.addFiles(entity.guid.get, createdBy, unmappedBAM.files)
        dataAccess.addMetadata(entity.guid.get, unmappedBAM.metadata)
        unmappedBAM.copy(id = entity.guid)
    }
  }

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
