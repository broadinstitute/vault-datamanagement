package org.broadinstitute.dsde.vault.datamanagement.domain

import org.broadinstitute.dsde.vault.datamanagement.util.Reflection
import org.broadinstitute.dsde.vault.datamanagement.domain.RelationKeyValue._

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

  private val metadataForEntity = Compiled(
    (entityGUID: Column[String]) => for {
      attribute <- attributes
      if attribute.entityGUID === entityGUID
    } yield (attribute.name, attribute.value))

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

  private val filesForEntity = Compiled(
    // Create a parameter for the entity GUID
    (entityGUID: Column[String]) => for {

    // Retrieve the relation where our "entityGUID" parameter is the entity1GUID
      relation <- relations
      if relation.entity1GUID === entityGUID

      // Using the foreign key, filter for the Relation.relation.entityType === FILE_TYPE.entityType
      typeEntity <- relation.relation
      if typeEntity.entityType === FILE_TYPE.entityType

      // Retrieve the attribute where:
      //   1) the Relation.relationGUID matches this attribute GUID
      //   2) this attribute name is FILE_TYPE.attributeName
      typeAttribute <- attributes
      if relation.relationGUID === typeAttribute.entityGUID &&
        typeAttribute.name === FILE_TYPE.attributeName

      // Using the foreign key, filter for the Relation.entity2.entityType === FILE_PATH.entityType
      pathEntity <- relation.entity2
      if pathEntity.entityType === FILE_PATH.entityType

      // Retrieve the attribute where:
      //   1) the Relation.entity2GUID matches this attribute GUID
      //   2) this attribute name is FILE_PATH.attributeName
      pathAttribute <- attributes
      if relation.entity2GUID === pathAttribute.entityGUID &&
        pathAttribute.name === FILE_PATH.attributeName

    } yield (typeAttribute.value, pathAttribute.value))

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
    val typeEntity = insertEntity(FILE_TYPE.entityType, createdBy)
    insertAttribute(typeEntity.guid.get, FILE_TYPE.attributeName, fileType)

    // Create an entity for the file path, with the path stored in an attribute
    val pathEntity = insertEntity(FILE_PATH.entityType, createdBy)
    insertAttribute(pathEntity.guid.get, FILE_PATH.attributeName, filePath)

    // Create a relation row with:
    //   2) The entity1 is the passed in original entity
    //   1) The relation is the file type of original entity
    //   3) The entity2 is the file path of the original entity
    insertRelation(typeEntity.guid.get, entityGUID, pathEntity.guid.get)
  }

  private val inputsForEntity = Compiled(
    // Create a parameter for the entity GUID
    (entityGUID: Column[String]) => for {

    // Retrieve the relation where our "entityGUID" parameter is the entity1GUID
      relation <- relations
      if relation.entity1GUID === entityGUID

      // Using the foreign key, filter for the Relation.relation.entityType === INPUT_TYPE.entityType
      typeEntity <- relation.relation
      if typeEntity.entityType === INPUT_TYPE.entityType

    } yield relation.entity2GUID)

  def getInputs(entityGUID: String)(implicit session: Session): Seq[String] = {
    inputsForEntity(entityGUID).list
  }

  def addInputs(entityGUID: String, createdBy: String, inputs: Seq[String])(implicit session: Session) {
    def addInputGuid(inputGuid: String) =
      addInput(entityGUID, createdBy, inputGuid)
    inputs.foreach(addInputGuid)
  }

  def addInput(entityGUID: String, createdBy: String, inputGuid: String)(implicit session: Session) {
    // Create an entity for the input
    val typeEntity = insertEntity(INPUT_TYPE.entityType, createdBy)

    // We count on the database to handle foreign key constraint errors for us. These will throw an exception
    // which will bubble up and result in the wrapping request returning an error.

    // Create a relation row with:
    //   2) The entity1 is the passed in original entity
    //   1) The relation is the input type we just created
    //   3) The entity2 is the id of the unmapped BAM specified by the user
    insertRelation(typeEntity.guid.get, entityGUID, inputGuid)
  }

  private val membersForEntity = Compiled(
    (entityGUID: Column[String]) => for {

     //Retrieve the relation where our "entityGUID" parameter is the entity1GUID
     relation <- relations
     if relation.entity1GUID === entityGUID

     // Using the foreign key, filter for the Relation.relation.entityType === MEMBER_TYPE.entityType
     typeEntity <- relation.relation
     if typeEntity.entityType === MEMBER_TYPE.entityType

    } yield (relation.entity2GUID))


  def getMembers(entityGUID: String)(implicit session: Session): Seq[String] = {
    membersForEntity(entityGUID).list
  }

  def addMembers(entityGUID: String, createdBy: String, members: Seq[String])(implicit session: Session) {
    def addMemberGuid(inputGuid: String) =
      addMember(entityGUID, createdBy, inputGuid)
    members.foreach(addMemberGuid)
  }

  def addMember(entityGUID: String, createdBy: String, memberGuid: String)(implicit session: Session) {
    // Create an entity for the member
    val typeEntity = insertEntity(MEMBER_TYPE.entityType, createdBy)

    // We count on the database to handle foreign key constraint errors for us. These will throw an exception
    // which will bubble up and result in the wrapping request returning an error.

    // Create a relation row with:
    //   2) The entity1 is the passed in original entity
    //   1) The relation is the input type we just created
    //   3) The entity2 is the id of the unmapped BAM specified by the user
    insertRelation(typeEntity.guid.get, entityGUID, memberGuid)
  }

  private val entityByTypeAttribute = Compiled(
    (entityType: Column[String],
     attributeName: Column[String],
     attributeValue: Column[String]) => for {
      entity <- entities
      if entity.entityType === entityType
      attribute <- attributes
      if entity.guid === attribute.entityGUID &&
        attribute.name === attributeName &&
        attribute.value === attributeValue
    } yield entity)

  def lookupEntityByTypeAttribute(entityType: String, attributeName: String, attributeValue: String)(implicit session: Session) = {
    entityByTypeAttribute((entityType, attributeName, attributeValue)).firstOption
  }
}
