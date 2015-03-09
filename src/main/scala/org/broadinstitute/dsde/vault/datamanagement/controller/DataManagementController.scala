package org.broadinstitute.dsde.vault.datamanagement.controller

import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.DatabaseConfig
import org.broadinstitute.dsde.vault.datamanagement.domain._

import scala.slick.jdbc.JdbcBackend._

object DataManagementController {
  val dataAccess = new DataAccess(DatabaseConfig.slickDriver)

  // TODO: Connection pooling
  def database: Database = Database.forURL(DatabaseConfig.jdbcUrl,
    DatabaseConfig.jdbcUser, DatabaseConfig.jdbcPassword,
    null, DatabaseConfig.jdbcDriver)

  def createUnmappedBAM(unmappedBAM: UnmappedBAM, createdBy: String): UnmappedBAM = {
     database withTransaction {
       implicit session =>
         val entity = dataAccess.insert(Entity("unmappedBAM", createdBy))
         dataAccess.addFiles(entity.guid, createdBy, unmappedBAM.files)
         dataAccess.addMetadata(entity.guid, unmappedBAM.metadata)
         unmappedBAM.copy(id = Option(entity.guid))
     }
  }

  def getUnmappedBAM(id: String): Option[UnmappedBAM] = {
    database withTransaction {
      implicit session =>
        dataAccess.getEntity(id).map(
          entity => {
            val files = dataAccess.getFiles(entity.guid)
            val metadata = dataAccess.getMetadata(entity.guid)
            UnmappedBAM(files, metadata, Option(entity.guid))
          }
        )
    }
  }

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
