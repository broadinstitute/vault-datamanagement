package org.broadinstitute.dsde.vault.datamanagement.controller.orient.obj

import com.orientechnologies.orient.`object`.db.{OObjectDatabasePool, OObjectDatabaseTx}
import org.broadinstitute.dsde.vault.datamanagement.controller.orient.OrientDataManagementController
import org.broadinstitute.dsde.vault.datamanagement.domain.orient.obj.OrientObjectDataAccess
import org.broadinstitute.dsde.vault.datamanagement.model._

/**
 * Uses OrientDB Object serialization to implement generic entity ingest and query.
 *
 * This API is simpler than TinkerPop Frames, as it allows serialization of embedded lists an maps.
 *
 * However, as the Object serialization is built on top of the Document API, it has the same limitations, mainly:
 * "Relationships are only Mono Directional. If you need Bidirectional relationships, it is your responsibility to maintain both LINKs."
 * - http://orientdb.com/docs/last/Choosing-between-Graph-or-Document-API.html
 */
object OrientObjectDataManagementController {

  private val pool = {
    // Create non-remote databases. Based on OrientGraphFactory.getDatabase()
    if (!OrientDataManagementController.url.startsWith("remote:")) {
      val db = new OObjectDatabaseTx(OrientDataManagementController.url)
      try {
        if (!db.exists) {
          db.create()
        }
      } finally {
        db.close()
      }
    }
    OObjectDatabasePool.global
  }

  def database = pool.acquire(
    OrientDataManagementController.url,
    OrientDataManagementController.username,
    OrientDataManagementController.password)

  initializeSchema()

  // TODO: More schema synchronization work necessary, for example
  // - Check if properties already set, causes indexes to be rebuilt!
  // - Other classes (Relationship) missing
  // - More indexes
  // - Should only be run like liquibase, during tests against memory db, or certain administrative upgrades
  private def initializeSchema(): Unit = {
    val db = database
    try {
      db.getEntityManager.registerEntityClasses(OrientObjectDataAccess.getClass.getPackage.getName + ".domain")
    } finally {
      db.close()
    }
  }

  private def withTransaction[T](f: OObjectDatabaseTx => T): T = {
    val db = database
    try {
      val result = f(db)
      db.commit()
      result
    } catch {
      case e: Throwable =>
        db.rollback()
        throw e
    } finally {
      db.close()
    }
  }

  def fetchEntity(guid: String): Option[GenericEntity] = {
    withTransaction { db =>
      OrientObjectDataAccess.fetchEntity(db, guid)
    }
  }

  def findDownstream(guid: String): Seq[GenericRelEnt] = {
    withTransaction { db =>
      OrientObjectDataAccess.findDownstream(db, guid)
    }
  }

  def findUpstream(guid: String): Seq[GenericRelEnt] = {
    withTransaction { db =>
      OrientObjectDataAccess.findUpstream(db, guid)
    }
  }

  def findEntitiesByTypeAndAttr(query: GenericEntityQuery): Seq[GenericEntity] = {
    withTransaction { db =>
      OrientObjectDataAccess.findEntitiesByTypeAndAttr(db, query)
    }
  }

  def ingestStuff(ingest: GenericIngest, createdBy: String): Seq[String] = {
    withTransaction { db =>
      OrientObjectDataAccess.ingestStuff(db, ingest, createdBy)
    }
  }
}
