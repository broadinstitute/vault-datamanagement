package org.broadinstitute.dsde.vault.datamanagement.domain

import java.sql.Timestamp
import java.util.UUID

import org.broadinstitute.dsde.vault.datamanagement.util.Reflection
import org.broadinstitute.dsde.vault.datamanagement.domain.RelationKeyValue._
import org.broadinstitute.dsde.vault.datamanagement.model._

import scala.collection.mutable
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

    } yield relation.entity2GUID)


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
      attr <- attributes
      if attr.name === attributeName && attr.value === attributeValue
      entity <- attr.entity
      if entity.entityType === entityType
    } yield entity )

  def lookupEntityByTypeAttribute(entityType: String, attributeName: String, attributeValue: String)(implicit session: Session) = {
    entityByTypeAttribute((entityType, attributeName, attributeValue)).firstOption
  }

  // query for downstream entities
  private val kids = Compiled(
    (guid: Column[String]) =>
      for {
        rel <- relations
        if rel.entity1GUID === guid
        selfEnt <- rel.relation
        targetEnt <- rel.entity2 } yield (selfEnt.guid,selfEnt.entityType,targetEnt) )

  private val kidAttrs = Compiled(
    (guid: Column[String]) =>
      for {
        rel <- relations
        if rel.entity1GUID === guid
        attr <- attributes
        if attr.entityGUID === rel.relationGUID ||
           attr.entityGUID === rel.entity2GUID } yield (attr.entityGUID, attr.name -> attr.value) )

  private def getAttrs(key: String, map: Option[Map[String,Seq[Tuple2[String,String]]]]): Option[Map[String,String]] = {
    if ( map.isEmpty ) None
    else if ( !map.get.contains(key) ) Some(Map.empty[String,String])
    else Some(map.get(key).toMap)
  }

  def findDownstream(guid: String)(implicit session: Session) = {
    val attrMap = Some(kidAttrs(guid).run groupBy {_._1} mapValues {_ map {_._2}})
    for ( entRel <- kids(guid).run )
      yield GenericRelEnt(
              GenericRelationship(entRel._2,getAttrs(entRel._1,attrMap)),
              GenericEntity(entRel._3.guid.get,entRel._3.entityType,
                            GenericSysAttrs(entRel._3.bossID,entRel._3.createdDate.get.getTime,entRel._3.createdBy,entRel._3.modifiedDate map {_.getTime},entRel._3.modifiedBy),
                            getAttrs(entRel._3.guid.get,attrMap)))
  }

  // query for upstream entities
  private val rents = Compiled(
    (guid: Column[String]) =>
      for {
        rel <- relations
        if rel.entity2GUID === guid
        selfEnt <- rel.relation
        targetEnt <- rel.entity1 } yield (selfEnt.guid,selfEnt.entityType,targetEnt) )

  private val rentAttrs = Compiled(
    (guid: Column[String]) =>
      for {
        rel <- relations
        if rel.entity2GUID === guid
        attr <- attributes
        if attr.entityGUID === rel.relationGUID ||
           attr.entityGUID === rel.entity1GUID } yield (attr.entityGUID, attr.name -> attr.value) )

  def findUpstream(guid: String)(implicit session: Session) = {
    val attrMap = Some(rentAttrs(guid).run groupBy {_._1} mapValues {_ map {_._2}})
    for ( entRel <- rents(guid).run )
      yield GenericRelEnt(
              GenericRelationship(entRel._2,getAttrs(entRel._1,attrMap)),
              GenericEntity(entRel._3.guid.get,entRel._3.entityType,
                            GenericSysAttrs(entRel._3.bossID,entRel._3.createdDate.get.getTime,entRel._3.createdBy,entRel._3.modifiedDate map {_.getTime},entRel._3.modifiedBy),
                            getAttrs(entRel._3.guid.get,attrMap)))
  }

  // query for a particular entity
  private val entityByGUID = Compiled(
    (guid: Column[String]) =>
      for {
        ent <- entities
        if ent.guid === guid } yield ent )

  private val attrsByGUID = Compiled(
    (guid: Column[String]) =>
      for {
        attr <- attributes
        if attr.entityGUID === guid } yield (attr.name, attr.value) )

  def fetchEntity(guid: String)(implicit session: Session) = {
    val entities =
      for ( ent <- entityByGUID(guid).run )
        yield GenericEntity(ent.guid.get,ent.entityType,
                            GenericSysAttrs(ent.bossID,ent.createdDate.get.getTime,ent.createdBy,ent.modifiedDate map {_.getTime},ent.modifiedBy),
                            Some(attrsByGUID(guid).run.toMap))
    assume(entities.size < 2, "query on unique vault ID returned multiple results")
    entities.headOption
  }

  // generic query
  def findEntities(query: GenericEntityQuery)(implicit session: Session) = {
    val bareEntityQuery = entities.filter(_.entityType === query.entityType)
    val entityQuery = query.attrSpec.foldLeft(bareEntityQuery)({(entQ,spec) => {
      val attrNVQuery = attributes.filter( attr => attr.name === spec.name && attr.value === spec.value )
      entQ innerJoin attrNVQuery on(_.guid === _.entityGUID) map(_._1) } })
    val attrMap =
      if ( !query.expandAttrs ) None
      else {
        val qa = entityQuery innerJoin attributes on(_.guid === _.entityGUID) map(_._2)
        Some(qa.run groupBy {_.entityGUID} mapValues(_ map { attr => (attr.name,attr.value) }))
      }
    for ( ent <- entityQuery.run )
        yield GenericEntity(ent.guid.get,ent.entityType,
                            GenericSysAttrs(ent.bossID,ent.createdDate.get.getTime,ent.createdBy,ent.modifiedDate map {_.getTime},ent.modifiedBy),
                            getAttrs(ent.guid.get,attrMap))
  }

  // generic ingestification
  val entRefPattern = "\\$(\\d+)".r

  def ingestStuff(ingest: GenericIngest, createdBy: String)(implicit session: Session) = {
    val now = Some(new Timestamp(System.currentTimeMillis))

    // transform List[GenericEntityIngest] into List[Entity]
    val genEnts = ingest.entities getOrElse List.empty[GenericEntityIngest]
    val ents = genEnts  map { genEnt => Entity(genEnt.entityType,createdBy,now,None,None,Some(UUID.randomUUID.toString),genEnt.bossID) }
    val entGUIDs = ents map { ent => ent.guid.get }

    // transform List[GenericRelationshipIngest] into List[Entity]
    val genRels = ingest.relations getOrElse List.empty[GenericRelationshipIngest]
    val relEnts = genRels map { genRel => Entity(genRel.relationType,createdBy,now,None,None,Some(UUID.randomUUID.toString)) }
    val allEnts = ents ++ relEnts

    // transform List[GenericRelationshipIngest] into List[Relationship]
    def resolve(guidOrRef: String) = { guidOrRef match { case entRefPattern(intStr) => entGUIDs(intStr.toInt) case _ => guidOrRef } }
    val rels = genRels.zipWithIndex map { case (genRel,idx) => Relation(relEnts(idx).guid.get,resolve(genRel.ent1),resolve(genRel.ent2)) }


    // handle attrs from entities and from relationships
    val entAttrs = genEnts zip entGUIDs flatMap { case (genEnt,guid) => for ( (name,value) <- genEnt.attrs ) yield Attribute(guid,name,value) }
    val relAttrs = genRels zip relEnts flatMap { case (genRel,relEnt) => for ( (name,value) <- genRel.attrs ) yield Attribute(relEnt.guid.get,name,value) }
    val allAttrs = entAttrs ++ relAttrs

    // do the DB work
    session withTransaction {
      entities ++= allEnts
      relations ++= rels
      attributes ++= allAttrs
    }

    // return GUIDs for new entities
    entGUIDs
  }
}
