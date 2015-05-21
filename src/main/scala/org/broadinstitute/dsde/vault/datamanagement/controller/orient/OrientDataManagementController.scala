package org.broadinstitute.dsde.vault.datamanagement.controller.orient

import com.orientechnologies.orient.core.collate.OCaseInsensitiveCollate
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx

import scala.collection.JavaConversions._

object OrientDataManagementController {

  //val url = DatabaseConfig.jdbcUrl
  //val username = DatabaseConfig.jdbcUser
  //val password = DatabaseConfig.jdbcPassword

  //val url = "remote:localhost/testdb"
  val url = "memory:testdb"
  val username = "admin"
  val password = "admin"

  initializeSchema()

  // TODO: More schema synchronization work necessary, for example
  // - Go down into Orient specific syntax to create lucene index
  // - Should only be run like liquibase, during tests against memory db, or certain administrative upgrades
  private def initializeSchema(): Unit = {
    val graph = new OrientGraphNoTx(url, username, password)
    val schema = graph.getRawGraph.getMetadata.getSchema
    val classes = Map(
      "Attribute" -> "V",
      "Entity" -> "V",
      "Relationship" -> "Entity")
    classes foreach { case (child, parent) =>
        if (schema.getClass(child) == null) {
          val parentClass = schema.getClass(parent)
          require(parentClass != null, s"Could not find parent class '$parent'")
          schema.createClass(child, parentClass)
        }
    }
    val entity = schema.getClass("entity")
    val nonNullProperties = Map(
      "guid" -> OType.STRING,
      "createdBy" -> OType.STRING,
      "createdDate" -> OType.DATETIME)
    nonNullProperties foreach { case (property, oType) =>
      if (entity.getProperty(property) == null) {
        entity.createProperty(property, oType).setNotNull(true).setReadonly(true)
      }
    }
    entity.getProperty("guid").setCollate(OCaseInsensitiveCollate.NAME)
    val requiredKeys = Set("guid", "entityType")
    val existingKeys = graph.getIndexedKeys(classOf[Vertex])
    requiredKeys &~ existingKeys foreach {
      key => graph.createKeyIndex(key, classOf[Vertex])
    }
    if (entity.getClassIndex("Entity.createdBy") == null) {
      // https://github.com/orientechnologies/orientdb-lucene/issues/45
      // aka https://github.com/orientechnologies/orientdb/issues/3863
      //entity.createIndex("Entity.createdBy", "FULLTEXT", null, null, "LUCENE", Array("createdBy"): _*)
      graph.command(new OCommandSQL("CREATE INDEX Entity.createdBy ON Entity (createdBy) FULLTEXT ENGINE LUCENE")).execute()
    }
    schema.save()
  }
}
