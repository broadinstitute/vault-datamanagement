package org.broadinstitute.dsde.vault.datamanagement.controller.orient.frames

import com.orientechnologies.orient.core.collate.OCaseInsensitiveCollate
import com.orientechnologies.orient.core.metadata.schema.OType
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientGraph, OrientGraphFactory}
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule
import com.tinkerpop.frames.{FramedGraph, FramedGraphFactory}
import org.broadinstitute.dsde.vault.datamanagement.controller.orient.OrientDataManagementController
import org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.{VertexCreator, FramesDataAccess}
import org.broadinstitute.dsde.vault.datamanagement.model.{GenericEntity, GenericEntityQuery, GenericIngest, GenericRelEnt}

import scala.collection.JavaConversions._

/**
 * Uses generic TinkerPop Frames serialization to implement generic entity ingest and query.
 *
 * This API is more generic than OrientDB Object serialization, and allows bidirectional navigation between entities.
 *
 * However, this comes at the cost of not being able to easily serialize OrientDB's collection types:
 * http://orientdb.com/docs/last/Types.html
 */
object FramesDataManagementController {
  private val factory = new FramedGraphFactory(new GremlinGroovyModule)
  private val pool = new OrientGraphFactory(
    OrientDataManagementController.url,
    OrientDataManagementController.username,
    OrientDataManagementController.password)

  initializeSchema()

  // TODO: Move schema synchronization work necessary, for example
  // - Go down into Orient specific syntax to create lucene index
  // - Should only be run like liquibase, during tests against memory db, or certain administrative upgrades
  private def initializeSchema(): Unit = {
    val graph = factory.create(pool.getNoTx)
    try {
      val baseGraph = graph.getBaseGraph
      val schema = baseGraph.getRawGraph.getMetadata.getSchema
      val root = schema.getClass("V")
      require(root != null, "did not get the root class V")
      val entity = schema.getClass("Entity")
      require(entity != null, "got null entity class")
      require(entity.getSuperClass != null, "got null superclass of entity")
      require(entity.getSuperClass.getName == "V", "unexpected root class of entity " + entity.getSuperClass.getName)
    } finally {
      graph.shutdown()
    }
  }

  private val dataAccess = new FramesDataAccess[OrientGraph](new VertexCreator[OrientGraph] {
    override def addVertex[T](graph: FramedGraph[OrientGraph], clazz: Class[T]) =
      graph.addVertex(OrientBaseGraph.CLASS_PREFIX + clazz.getSimpleName, clazz)
  })

  private def withTransaction[T](f: FramedGraph[OrientGraph] => T): T = {
    val db = factory.create(pool.getTx)
    try {
      val result = f(db)
      db.commit()
      result
    } catch {
      case e: Throwable =>
        db.rollback()
        throw e
    } finally {
      db.shutdown()
    }
  }

  def fetchEntity(guid: String): Option[GenericEntity] = {
    withTransaction { db =>
      dataAccess.fetchEntity(db, guid)
    }
  }

  def findDownstream(guid: String): Seq[GenericRelEnt] = {
    withTransaction { db =>
      dataAccess.findDownstream(db, guid)
    }
  }

  def findUpstream(guid: String): Seq[GenericRelEnt] = {
    withTransaction { db =>
      dataAccess.findUpstream(db, guid)
    }
  }

  def findEntitiesByTypeAndAttr(query: GenericEntityQuery): Seq[GenericEntity] = {
    withTransaction { db =>
      dataAccess.findEntitiesByTypeAndAttr(db, query)
    }
  }

  def ingestStuff(ingest: GenericIngest, createdBy: String): Seq[String] = {
    withTransaction { db =>
      dataAccess.ingestStuff(db, ingest, createdBy)
    }
  }

}
