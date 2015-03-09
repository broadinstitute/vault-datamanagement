package org.broadinstitute.dsde.vault.datamanagement.domain

import org.broadinstitute.dsde.vault.datamanagement.util.Reflection

import scala.slick.driver.JdbcProfile

class DataAccess(val driver: JdbcProfile)
  extends AttributeComponent
  with EntityComponent
  with RelationComponent
  with DriverComponent {

  import driver.simple._

  def this(driverName: String) {
    this(Reflection.getObject[JdbcProfile](driverName))
  }

  def getMetadata(entityGUID: String)(implicit session: Session): Map[String, String] = {
    val metadataForEntity = for {
      eg <- Parameters[String]
      a <- attributes if a.entityGUID === eg
    } yield (a.name, a.value)
    metadataForEntity(entityGUID).list.toMap
  }

  def addMetadata(entityGUID: String, metadata: Map[String, String])(implicit session: Session) {
    metadata.foreach{case (name, value) =>
      insert(Attribute(entityGUID, name, value))
    }
  }

  def getFiles(entityGUID: String)(implicit session: Session): Map[String, String] = {
    val filesForEntity = for {
      eg <- Parameters[String]
      r <- relations if r.entity1GUID === eg
      na <- attributes if r.relationGUID === na.entityGUID && na.name === "name"
      pa <- attributes if r.entity2GUID === pa.entityGUID && pa.name === "path"
      // TODO: Also validate relation entity types?
      // Without the filter more rows may be returned than expected.
    } yield (na.value, pa.value)
    filesForEntity(entityGUID).list.toMap
  }

  def addFiles(entityGUID: String, createdBy: String, files: Map[String, String])(implicit session: Session) {
    files.foreach{case (fileType, path) =>
      val typeEntity = insert(Entity("fileType", createdBy))
      insert(Attribute(typeEntity.guid, "name", fileType))
      val fileEntity = insert(Entity("file", createdBy))
      insert(Attribute(fileEntity.guid, "path", path))
      insert(Relation(typeEntity.guid, entityGUID, fileEntity.guid))
    }
  }
}
