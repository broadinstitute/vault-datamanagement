package org.broadinstitute.dsde.vault.datamanagement.controller

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.DatabaseConfig
import org.broadinstitute.dsde.vault.datamanagement.domain._
import org.broadinstitute.dsde.vault.datamanagement.model._

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

  // ==================== unmapped BAMS ====================
  def createUnmappedBAM(unmappedBAM: UnmappedBAM, createdBy: String): UnmappedBAM = {
    database withTransaction {
      implicit session =>
        val entity = dataAccess.insertEntity(EntityType.UNMAPPED_BAM.databaseKey, createdBy)
        dataAccess.addFiles(entity.guid.get, createdBy, unmappedBAM.files)
        dataAccess.addMetadata(entity.guid.get, unmappedBAM.metadata)
        unmappedBAM.copy(properties = getProperties(entity), id = entity.guid)
    }
  }

  def getUnmappedBAM(id: String): Option[UnmappedBAM] = {
    database withTransaction {
      implicit session =>
        dataAccess.getEntity(id).map(
          entity => {
            val files = dataAccess.getFiles(entity.guid.get)
            val metadata = dataAccess.getMetadata(entity.guid.get)
            UnmappedBAM(files, metadata, getProperties(entity), entity.guid)
          }
        )
    }
  }

  // ==================== analyses ====================
  def createAnalysis(analysis: Analysis, createdBy: String): Analysis = {
    database withTransaction {
      implicit session =>
        val entity = dataAccess.insertEntity(EntityType.ANALYSIS.databaseKey, createdBy)
        dataAccess.addInputs(entity.guid.get, createdBy, analysis.input.get)
        // in the main Apollo use case, files will always be empty, so the following line is a no-op.
        // leaving it here in case the use case changes in the future.
        analysis.files map { files => dataAccess.addFiles(entity.guid.get, createdBy, files)}
        dataAccess.addMetadata(entity.guid.get, analysis.metadata.get)
        analysis.copy(properties = getProperties(entity), id = entity.guid)
    }
  }

  def getAnalysis(id: String): Option[Analysis] = {
    database withTransaction {
      implicit session =>
        getAnalysisWithSession(id)
    }
  }

  def completeAnalysis(id: String, files: Map[String, String], updatedBy: String): Option[Analysis] = {
    database withTransaction {
      implicit session =>
        dataAccess.updateEntity(id, updatedBy)
        dataAccess.addFiles(id, updatedBy, files)
        getAnalysisWithSession(id)
    }
  }

  // ==================== common utility methods ====================
  private def getAnalysisWithSession(id: String)(implicit session: Session): Option[Analysis] = {
    dataAccess.getEntity(id).map(
      entity => {
        val input = dataAccess.getInputs(entity.guid.get)
        val files = dataAccess.getFiles(entity.guid.get)
        val metadata = dataAccess.getMetadata(entity.guid.get)
        Analysis(Option(input), Option(metadata), Option(files), getProperties(entity), entity.guid)
      }
    )
  }
  
  private def getProperties(entity: Entity): Option[Map[String, String]] = {
    import org.broadinstitute.dsde.vault.datamanagement.model.Properties._
    val pairs = List((created_by, Option(entity.createdBy)),
                     (created_date, entity.createdDate.map(_.getTime.toString)),
                     (modified_by, entity.modifiedBy),
                     (modified_date, entity.modifiedDate.map(_.getTime.toString)))
    Some(pairs.filter(_._2.isDefined).map(p => (p._1, p._2.get)).toMap)
  }

  // ==================== test methods ====================
  def getEntity(id: String) = {
    database withTransaction {
      implicit session =>
        dataAccess.getEntity(id)
    }
  }

  // ==================== lookup service ====================

  def lookupEntityByEndpointAttribute
  (endpoint: String, attributeName: String, attributeValue: String): Option[EntitySearchResult] = {
    database withTransaction {
      implicit session =>
        EntityType.TYPES.find(_.endpoint == endpoint) match {
          case Some(entityType) =>
            dataAccess
              .lookupEntityByTypeAttribute(entityType.databaseKey, attributeName, attributeValue)
              .map(entity => EntitySearchResult(entity.guid.get, entityType.endpoint))
          case None => None
        }
    }
  }

}
