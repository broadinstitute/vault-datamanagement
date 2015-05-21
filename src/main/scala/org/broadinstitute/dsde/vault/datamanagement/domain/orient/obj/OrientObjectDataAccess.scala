package org.broadinstitute.dsde.vault.datamanagement.domain.orient.obj

import java.util.{UUID, Date}

import com.orientechnologies.orient.`object`.db.OObjectDatabaseTx
import com.orientechnologies.orient.core.collate.OCaseInsensitiveCollate
import com.orientechnologies.orient.core.command.OCommandRequest
import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import org.broadinstitute.dsde.vault.datamanagement.domain.orient.obj.domain.{Attribute, Relationship, Entity}
import org.broadinstitute.dsde.vault.datamanagement.model._
import scala.collection.JavaConversions._

object OrientObjectDataAccess {
  def initializeSchema(db: OObjectDatabaseTx): Unit = {
  }

  def fetchEntity(db: OObjectDatabaseTx, guid: String): Option[GenericEntity] = {
    val result = db.query[java.util.List[Entity]](new OSQLSynchQuery[Entity](
      "select from Entity where guid = ?"), guid)
    result.headOption map toGenericEntity
  }

  def findDownstream(db: OObjectDatabaseTx, guid: String): Seq[GenericRelEnt] = {
    val result = db.query[java.util.List[Relationship]](new OSQLSynchQuery[Relationship](
    // Fetch everything EXCEPT entity1
      "select from Relationship where entity1.guid = ?").setFetchPlan("*:-1 entity1:0"), guid)
    result map { relationship =>
      val entity = relationship.entity2
      GenericRelEnt(
        relationship = GenericRelationship(
          relationship.entityType,
          attrs = Option(relationship.attributes.map(attr => attr.name -> attr.value).toMap)),
        entity = toGenericEntity(entity))
    }
  }

  def findUpstream(db: OObjectDatabaseTx, guid: String): Seq[GenericRelEnt] = {
    val result = db.query[java.util.List[Relationship]](new OSQLSynchQuery[Relationship](
      // Fetch everything EXCEPT entity2
      "select from Relationship where entity2.guid = ?").setFetchPlan("*:-1 entity2:0"), guid)
    result map { relationship =>
      val entity = relationship.entity1
      GenericRelEnt(
        relationship = GenericRelationship(
          relationship.entityType,
          attrs = Option(relationship.attributes.map(attr => attr.name -> attr.value).toMap)),
        entity = toGenericEntity(entity))
    }
  }

  def findEntitiesByTypeAndAttr(db: OObjectDatabaseTx, query: GenericEntityQuery): Seq[GenericEntity] = {
    val result = db.query[java.util.List[Entity]](new OSQLSynchQuery[Entity](
      query.attrSpec.map(attr =>
        " and attributes contains (name = ? and value = ?)"
      ).mkString("select from Entity where entityType = ?", "", "")).setFetchPlan(
        if (query.expandAttrs) "*:-1" else null
      ), Seq(query.entityType) ++ query.attrSpec.flatMap(attr => Seq(attr.name, attr.value)): _*)
    result map {
      toGenericEntity(_, query.expandAttrs)
    }
  }

  // generic ingestification
  val entRefPattern = "\\$(\\d+)".r

  def ingestStuff(db: OObjectDatabaseTx, ingest: GenericIngest, createdBy: String): Seq[String] = {
    val now = new Date()
    val entities = ingest.entities map (_.toSeq) getOrElse Seq.empty map { entityIngest =>
      val entity = db.newInstance[Entity](classOf[Entity])
      entity.guid = UUID.randomUUID().toString
      entity.entityType = entityIngest.entityType
      entity.createdBy = createdBy
      entity.createdDate = now
      entity.bossID = entityIngest.bossID.orNull
      entity.attributes = entityIngest.attrs.toSeq map { case (name, value) =>
        val attr = db.newInstance[Attribute](classOf[Attribute])
        attr.name = name
        attr.value = value
        db.attachAndSave[Attribute](attr)
      }
      val newEntity = db.attachAndSave[Entity](entity)
      newEntity
    }

    def resolve(guidOrRef: String): Entity = {
      guidOrRef match {
        case entRefPattern(intStr) =>
          // Return ordinal entity from this ingest
          entities(intStr.toInt)
        case guid =>
          // Go to the cache, or all the way to the database to get the entity proxy object for linking.
          // TODO: Can we use a fetch plan to just get the ID?
          // TODO: If we weren't using object mapping then we could just use the ID?
          db.query[java.util.List[Entity]](new OSQLSynchQuery[Entity](
            "select from Entity where guid = ?"), guid).head
      }
    }

    val relationships = ingest.relations map (_.toSeq) getOrElse Seq.empty map { relationshipIngest =>
      val relationship = db.newInstance[Relationship](classOf[Relationship])
      relationship.guid = UUID.randomUUID().toString
      relationship.entityType = relationshipIngest.relationType
      relationship.createdBy = createdBy
      relationship.createdDate = now
      relationship.entity1 = resolve(relationshipIngest.ent1)
      relationship.entity2 = resolve(relationshipIngest.ent2)
      relationship.attributes = relationshipIngest.attrs.toSeq map { case (name, value) =>
        val attr = db.newInstance[Attribute](classOf[Attribute])
        attr.name = name
        attr.value = value
        db.attachAndSave[Attribute](attr)
      }
      db.attachAndSave[Relationship](relationship)
    }

    entities map (_.guid)
  }

  private def toGenericEntity(entity: Entity): GenericEntity = {
    toGenericEntity(entity, expandAttrs = true)
  }

  private def toGenericEntity(entity: Entity, expandAttrs: Boolean): GenericEntity = {
    GenericEntity(
      guid = entity.guid,
      entityType = entity.entityType,
      sysAttrs = GenericSysAttrs(
        Option(entity.bossID),
        entity.createdDate.getTime,
        entity.createdBy,
        Option(entity.modifiedDate).map(_.getTime),
        Option(entity.modifiedBy)),
      attrs = if (expandAttrs) Option(entity.attributes.map(attr => attr.name -> attr.value).toMap) else None)
  }
}
