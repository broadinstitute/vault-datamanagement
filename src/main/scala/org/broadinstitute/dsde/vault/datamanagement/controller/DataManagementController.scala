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
  DatabaseConfig.c3p0MaxStatementsOption.foreach(comboPooledDataSource.setMaxStatements)

  def database: Database = Database.forDataSource(comboPooledDataSource)

  // ==================== unmapped BAMS ====================
  def createUnmappedBAM(unmappedBAM: UnmappedBAM, createdBy: String, includeProperties: Boolean): UnmappedBAM = {
    database withTransaction {
      implicit session =>
        val entity = dataAccess.insertEntity(EntityType.UNMAPPED_BAM.databaseKey, createdBy)
        dataAccess.addFiles(entity.guid.get, createdBy, unmappedBAM.files)
        dataAccess.addMetadata(entity.guid.get, unmappedBAM.metadata)
        unmappedBAM.copy(properties = getProperties(entity, includeProperties), id = entity.guid)
    }
  }

  def getUnmappedBAM(id: String, includeProperties: Boolean): Option[UnmappedBAM] = {
    database withTransaction {
      implicit session =>
        dataAccess.getEntity(id).map(
          entity => {
            val files = dataAccess.getFiles(entity.guid.get)
            val metadata = dataAccess.getMetadata(entity.guid.get)
            val properties = getProperties(entity, includeProperties)
            UnmappedBAM(files, metadata, properties, entity.guid)
          }
        )
    }
  }

  def getUnmappedBAMList(includeProperties: Boolean, pageLimit: Option[Int]): List[UnmappedBAM] = {
    database withTransaction {
      implicit session =>
        dataAccess.getEntityList(pageLimit).map(
          entity => {
            val files = dataAccess.getFiles(entity.guid.get)
            val metadata = dataAccess.getMetadata(entity.guid.get)
            val properties = getProperties(entity, includeProperties)
            UnmappedBAM(files, metadata, properties, entity.guid)
          }
        )
    }
  }

  // ==================== analyses ====================
  def createAnalysis(analysis: Analysis, createdBy: String, includeProperties: Boolean): Analysis = {
    database withTransaction {
      implicit session =>
        val entity = dataAccess.insertEntity(EntityType.ANALYSIS.databaseKey, createdBy)
        dataAccess.addInputs(entity.guid.get, createdBy, analysis.input.get)
        // in the main Apollo use case, files will always be empty, so the following line is a no-op.
        // leaving it here in case the use case changes in the future.
        analysis.files foreach { files => dataAccess.addFiles(entity.guid.get, createdBy, files) }
        dataAccess.addMetadata(entity.guid.get, analysis.metadata.get)
        analysis.copy(properties = getProperties(entity, includeProperties), id = entity.guid)
    }
  }

  def getAnalysis(id: String, includeProperties: Boolean): Option[Analysis] = {
    database withTransaction {
      implicit session =>
        getAnalysisWithSession(id, includeProperties)
    }
  }

  def completeAnalysis(id: String, files: Map[String, String], updatedBy: String, includeProperties: Boolean): Option[Analysis] = {
    database withTransaction {
      implicit session =>
        dataAccess.updateEntity(id, updatedBy)
        dataAccess.addFiles(id, updatedBy, files)
        getAnalysisWithSession(id, includeProperties)
    }
  }

  // ================= UBam Collections methods =================
  def createUBAMCollection(collection: UBamCollection, createdBy: String, version: Int): UBamCollection ={
    database withTransaction {
      implicit session =>
        val entity = dataAccess.insertEntity(EntityType.UBAM_COLLECTION.databaseKey, createdBy)
        dataAccess.addMetadata(entity.guid.get, collection.metadata.get)
        dataAccess.addMembers(entity.guid.get, createdBy, collection.members.get)
        collection.copy(properties = Option(getProperties(entity)), id = entity.guid)
    }
  }

  def getUBAMCollection(id: String): Option[UBamCollection] = {
    database withTransaction {
      implicit session =>
        dataAccess.getEntity(id).map(
          entity => {
            val members = dataAccess.getMembers(entity.guid.get)
            val metadata = dataAccess.getMetadata(entity.guid.get)
            UBamCollection(Option(members), Option(metadata), Option(getProperties(entity)), entity.guid)
          }
        )
    }
  }

  // ==================== common utility methods ====================
  private def getAnalysisWithSession(id: String, includeProperties: Boolean)(implicit session: Session): Option[Analysis] = {
    dataAccess.getEntity(id).map(
      entity => {
        val input = dataAccess.getInputs(entity.guid.get)
        val files = dataAccess.getFiles(entity.guid.get)
        val metadata = dataAccess.getMetadata(entity.guid.get)
        Analysis(Option(input), Option(metadata), Option(files), getProperties(entity, includeProperties), entity.guid)
      }
    )
  }

  private def getProperties(entity: Entity): Map[String, String] = {
    import org.broadinstitute.dsde.vault.datamanagement.model.Properties._
    val pairs = List((CreatedBy, Option(entity.createdBy)),
      (CreatedDate, entity.createdDate.map(_.getTime.toString)),
      (ModifiedBy, entity.modifiedBy),
      (ModifiedDate, entity.modifiedDate.map(_.getTime.toString)))
    pairs.filter(_._2.isDefined).map(p => (p._1, p._2.get)).toMap
  }

  private def getProperties(entity: Entity, includeProperties: Boolean): Option[Map[String, String]] =
    if (includeProperties) Option(getProperties(entity)) else None

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

  // ==================== test methods ====================
  def getEntity(id: String) = {
    database withTransaction {
      implicit session =>
        dataAccess.getEntity(id)
    }
  }

  // ==================== generic entity service methods ====================
  def findDownstream(guid: String) = {
    database withSession {
      implicit session => dataAccess.findDownstream(guid)
    }
  }

  def findUpstream(guid: String) = {
    database withSession {
      implicit session => dataAccess.findUpstream(guid)
    }
  }

  def fetchEntity(guid: String) = {
    database withSession {
      implicit session => dataAccess.fetchEntity(guid)
    }
  }

  def findEntitiesByTypeAndAttr(query: GenericEntityQuery) = {
    database withSession {
      implicit session => dataAccess.findEntities(query)
    }
  }

  def ingestStuff(ingest: GenericIngest, createdBy: String) = {
    database withSession {
      implicit session => dataAccess.ingestStuff(ingest, createdBy)
    }
  }
}
