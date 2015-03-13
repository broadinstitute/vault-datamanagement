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

  // Using Slick 1.0 syntax because it's shorter
  private val metadataForEntity = for {
    entityGUID <- Parameters[String]
    attribute <- attributes
    if attribute.entityGUID === entityGUID
  } yield (attribute.name, attribute.value)

  def getMetadata(entityGUID: String)(implicit session: Session): Map[String, String] = {
    metadataForEntity(entityGUID).toMap
  }

  def addMetadata(entityGUID: String, metadata: Map[String, String])(implicit session: Session) {
    def addMetadatumNameValue(name: String, value: String) =
      addMetadatum(entityGUID, name, value)
    metadata.foreach(Function.tupled(addMetadatumNameValue))
  }

  def addMetadatum(entityGUID: String, name: String, value: String)(implicit session: Session) {
    insertAttribute(entityGUID, name, value)
  }

  // Using Slick 1.0 syntax because it's shorter
  private val filesForEntity = for {
  // Create a parameter for the entity GUID
    entityGUID <- Parameters[String]

    // Retrieve the relation where our "entityGUID" parameter is the entity1GUID
    relation <- relations
    if relation.entity1GUID === entityGUID

    // Retrieve the attribute where:
    //   1) the Relation.relationGUID matches this attribute GUID
    //   2) this attribute name is "name"
    nameAttribute <- attributes
    if relation.relationGUID === nameAttribute.entityGUID &&
      nameAttribute.name === "name"

    // Retrieve the attribute where:
    //   1) the Relation.entity2GUID matches this attribute GUID
    //   2) this attribute name is "path"
    pathAttribute <- attributes
    if relation.entity2GUID === pathAttribute.entityGUID &&
      pathAttribute.name === "path"

    // Using the foreign key, filter for the Relation.relation.entityType === "fileType"
    typeEntity <- relation.relation
    if typeEntity.entityType === "fileType"

    // Using the foreign key, filter for the Relation.entity2.entityType === "file"
    fileEntity <- relation.entity2
    if fileEntity.entityType === "file"

  } yield (nameAttribute.value, pathAttribute.value)

  def getFiles(entityGUID: String)(implicit session: Session): Map[String, String] = {
    filesForEntity(entityGUID).toMap
  }

  def addFiles(entityGUID: String, createdBy: String, files: Map[String, String])(implicit session: Session) {
    def addFileTypePath(fileType: String, filePath: String) =
      addFile(entityGUID, createdBy, fileType, filePath)
    files.foreach(Function.tupled(addFileTypePath))
  }

  def addFile(entityGUID: String, createdBy: String, fileType: String, filePath: String)(implicit session: Session) {
    // Create an entity for the type, with an attribute name that stores said file type
    val typeEntity = insertEntity("fileType", createdBy)
    insertAttribute(typeEntity.guid.get, "name", fileType)

    // Create an entity for the file path, with the path stored in an attribute
    val fileEntity = insertEntity("file", createdBy)
    insertAttribute(fileEntity.guid.get, "path", filePath)

    // Create a relation row with:
    //   2) The entity1 is the passed in original entity
    //   1) The relation is the file type of original entity
    //   3) The entity2 is the file path of the original entity
    insertRelation(typeEntity.guid.get, entityGUID, fileEntity.guid.get)
  }
}
