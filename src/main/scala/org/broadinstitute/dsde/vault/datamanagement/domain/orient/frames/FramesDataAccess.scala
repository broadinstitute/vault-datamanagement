package org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames

import java.util.{Date, UUID}

import com.tinkerpop.blueprints.{Graph, Vertex}
import com.tinkerpop.frames.FramedGraph
import com.tinkerpop.gremlin.java.GremlinPipeline
import org.broadinstitute.dsde.vault.datamanagement.model._

import scala.collection.JavaConversions._

trait VertexCreator[BaseGraph <: Graph] {
  def addVertex[T](graph: FramedGraph[BaseGraph], clazz: Class[T]): T
}

class FramesDataAccess[BaseGraph <: Graph](vertexCreator: VertexCreator[BaseGraph]) {
  def fetchEntity(graph: FramedGraph[BaseGraph], guid: String): Option[GenericEntity] = {
    graph.getVertices("guid", guid).headOption map { vertex => toGenericEntity(graph, vertex, expandAttrs = true) }
  }

  def findDownstream(graph: FramedGraph[BaseGraph], guid: String): Seq[GenericRelEnt] = {
    graph.getVertices("guid", guid).headOption.toSeq flatMap {
      vertex => graph.frame(vertex, classOf[Entity]).getChildRelationships
    } map { relationship =>
      val entity = relationship.getEntity2
      GenericRelEnt(
        relationship = GenericRelationship(
          relationship.getEntityType,
          attrs = Option(relationship.getAttributes.map(attr => attr.getName -> attr.getValue).toMap)),
        entity = toGenericEntity(entity))
    }
  }

  def findUpstream(graph: FramedGraph[BaseGraph], guid: String): Seq[GenericRelEnt] = {
    graph.getVertices("guid", guid).headOption.toSeq flatMap {
      vertex => graph.frame(vertex, classOf[Entity]).getParentRelationships
    } map { relationship =>
      val entity = relationship.getEntity1
      GenericRelEnt(
        relationship = GenericRelationship(
          relationship.getEntityType,
          attrs = Option(relationship.getAttributes.map(attr => attr.getName -> attr.getValue).toMap)),
        entity = toGenericEntity(entity))
    }
  }

  def findEntitiesByTypeAndAttr(graph: FramedGraph[BaseGraph], query: GenericEntityQuery): Seq[GenericEntity] = {
    query.attrSpec.foldLeft(
      new GremlinPipeline(graph).V().has(
        "entityType", query.entityType
      ).as("entity").cast(classOf[Vertex])) {
      case (pipeline, attr) =>
        pipeline.out("hasAttribute").has("name", attr.name).has("value", attr.value).back("entity").cast(classOf[Vertex])
    }.toList map { vertex =>
      toGenericEntity(graph, vertex, query.expandAttrs)
    }
  }

  // generic ingestification
  val entRefPattern = "\\$(\\d+)".r

  def ingestStuff(graph: FramedGraph[BaseGraph], ingest: GenericIngest, createdBy: String): Seq[String] = {
    val now = new Date()
    val entities = ingest.entities map (_.toSeq) getOrElse Seq.empty map { entityIngest =>
      val entity = vertexCreator.addVertex(graph, classOf[Entity])
      entity.setGuid(UUID.randomUUID().toString)
      entity.setEntityType(entityIngest.entityType)
      entity.setCreatedBy(createdBy)
      entity.setCreatedDate(now)
      entity.setBossID(entityIngest.bossID.orNull)
      entityIngest.attrs.toSeq foreach { case (name, value) =>
        val attr = vertexCreator.addVertex(graph, classOf[Attribute])
        attr.setName(name)
        attr.setValue(value)
        entity.addAttribute(attr)
      }
      entity
    }

    def resolve(guidOrRef: String): Entity = {
      guidOrRef match {
        case entRefPattern(intStr) =>
          // Return ordinal entity from this ingest
          entities(intStr.toInt)
        case guid =>
          // Go to the cache, or all the way to the database to get the entity proxy object for linking.
          graph.frame(graph.getVertices("guid", guid).head, classOf[Entity])
      }
    }

    val relationships = ingest.relations map (_.toSeq) getOrElse Seq.empty map { relationshipIngest =>
      val relationship = vertexCreator.addVertex(graph, classOf[Relationship])
      relationship.setGuid(UUID.randomUUID().toString)
      relationship.setEntityType(relationshipIngest.relationType)
      relationship.setCreatedBy(createdBy)
      relationship.setCreatedDate(now)
      relationship.setEntity1(resolve(relationshipIngest.ent1))
      relationship.setEntity2(resolve(relationshipIngest.ent2))
      relationshipIngest.attrs.toSeq foreach { case (name, value) =>
        val attr = vertexCreator.addVertex(graph, classOf[Attribute])
        attr.setName(name)
        attr.setValue(value)
        relationship.addAttribute(attr)
      }
      relationship
    }

    entities map (_.getGuid)
  }

  private def toGenericEntity(graph: FramedGraph[BaseGraph], vertex: Vertex, expandAttrs: Boolean): GenericEntity = {
    toGenericEntity(graph.frame(vertex, classOf[Entity]), expandAttrs)
  }

  private def toGenericEntity(entity: Entity): GenericEntity = {
    toGenericEntity(entity, expandAttrs = true)
  }

  private def toGenericEntity(entity: Entity, expandAttrs: Boolean): GenericEntity = {
    GenericEntity(
      guid = entity.getGuid,
      entityType = entity.getEntityType,
      sysAttrs = GenericSysAttrs(
        Option(entity.getBossID),
        entity.getCreatedDate.getTime,
        entity.getCreatedBy,
        Option(entity.getModifiedDate).map(_.getTime),
        Option(entity.getModifiedBy)),
      attrs = if (expandAttrs) Option(entity.getAttributes.map(attr => attr.getName -> attr.getValue).toMap) else None)
  }

}
