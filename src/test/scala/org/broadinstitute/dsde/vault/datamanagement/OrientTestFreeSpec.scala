package org.broadinstitute.dsde.vault.datamanagement

import java.sql.Timestamp
import java.util.UUID

import com.orientechnologies.orient.`object`.db.OObjectDatabaseTx
import com.orientechnologies.orient.core.collate.OCaseInsensitiveCollate
import com.orientechnologies.orient.core.command.OCommandRequest
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.impls.orient.{OrientGraphNoTx, OrientGraph, OrientGraphFactory}
import com.tinkerpop.blueprints.{Direction, Vertex}
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule
import com.tinkerpop.frames.{FramedGraphFactory, Property}
import org.broadinstitute.dsde.vault.datamanagement.controller.orient.obj.OrientObjectDataManagementController
import org.broadinstitute.dsde.vault.datamanagement.domain.orient.obj.domain.{Attribute, Entity, Relationship}

import scala.collection.JavaConversions._

/*

VARIOUS ORIENTDB DEBUGGING UTILITIES.
SHOULD NOT BE ADDED TO MASTER!

 */


class OrientTestFreeSpec extends DataManagementFreeSpec {

  def opendb: OObjectDatabaseTx = OrientObjectDataManagementController.database

  "OrientDB" - {
    val guid = UUID.randomUUID.toString

    "insert" in {
      val db = opendb
      try {
        val entity1 = db.newInstance(classOf[Entity])
        entity1.guid = UUID.randomUUID().toString
        entity1.createdBy = "parent"
        entity1.createdDate = new Timestamp(0L)
        entity1.attributes = Seq(db.newInstance[Attribute](classOf[Attribute]))
        entity1.attributes.head.name = "parentKey"
        entity1.attributes.head.value = "parentValue"
        entity1.attributes = entity1.attributes.map(db.attachAndSave[Attribute](_))
        val e1 = db.attachAndSave[Entity](entity1)

        val entity2 = db.newInstance(classOf[Entity])
        entity2.guid = guid
        entity2.createdBy = "child"
        entity2.createdDate = new Timestamp(0L)
        entity2.attributes = Seq(db.newInstance[Attribute](classOf[Attribute]))
        entity2.attributes.head.name = "childKey"
        entity2.attributes.head.value = "childValue"
        entity2.attributes = entity2.attributes.map(db.attachAndSave[Attribute](_))
        val e2 = db.attachAndSave[Entity](entity2)

        val relationship = db.newInstance(classOf[Relationship])
        relationship.guid = UUID.randomUUID().toString
        relationship.createdBy = "relationship"
        relationship.createdDate = new Timestamp(0L)
        relationship.attributes = Seq(db.newInstance[Attribute](classOf[Attribute]))
        relationship.attributes.head.name = "relationshipKey"
        relationship.attributes.head.value = "relationshipValue"
        relationship.attributes = relationship.attributes.map(db.attachAndSave[Attribute](_))
        relationship.entity1 = e1
        relationship.entity2 = e2
        relationship.entityType = "TestRelation"
        val r2 = db.attachAndSave[Relationship](relationship)

      } finally {
        db.close()
      }
    }
    "retrieve" in {
      var url: String = null
      var guid2: String = null

      val db = opendb
      try {
        val scalaResult = db.query[java.util.List[Relationship]](new OSQLSynchQuery[Relationship](s"select * from Relationship where entity2.guid = ?").setFetchPlan("*:-1"), guid)//guid.take(4) + "*")
        val head = scalaResult.head
        println("attributes = " + head.attributes)
        println("attributes.head.name = " + head.attributes.head.name)
        println("attributes.head.value = " + head.attributes.head.value)
        println("entity1 = " + head.entity1)
        println("entity1.attributes = " + head.entity1.attributes)
        //println(scalaResult.head.attributes("testKey"))

        val scalaResult2 = db.query[java.util.List[Entity]](new OSQLSynchQuery[Entity](s"select * from Entity where entity2.guid = ?"), guid)//guid.take(4) + "*")
        val head2 = scalaResult2.head
        println("attributes = " + head2.attributes)
        println("attributes.head.name = " + head2.attributes.head.name)
        println("attributes.head.value = " + head2.attributes.head.value)
        //println("entity1 = " + head2.entity1)
        //println("entity1.attributes = " + head2.entity1.attributes)

        url = db.getURL
        guid2 = head2.asInstanceOf[Relationship].guid

        val scalaResult3 = db.query[java.util.List[Entity]](new OSQLSynchQuery[Entity](s"select * from Entity where createdBy LUCENE ?"), "relationshi*")
        val head3 = scalaResult3.head
        println("attributes = " + head3.attributes)
        println("attributes.head.name = " + head3.attributes.head.name)
        println("attributes.head.value = " + head3.attributes.head.value)
      } finally {
        db.close()
      }

      trait FramedEntityBase {
        @Property("name")
        def setName(name: String): Unit

        @Property("name")
        def getName: String

        @Property("height")
        def setHeight(height: Int)

        @Property("height")
        def getHeight: Int

        @Property("dob")
        def setDob(height: java.util.Date)

        @Property("dob")
        def getDob: java.util.Date
      }

      trait FramedEntity extends FramedEntityBase {
        @Property("age")
        def setAge(name: Int): Unit

        @Property("age")
        def getAge: Int
      }

      val graph2: OrientGraph = new OrientGraphFactory(url, "admin", "admin").getTx
      val factory = new FramedGraphFactory(new GremlinGroovyModule)
      try {
        val framedGraph = factory.create(graph2)

        val entity1Attr = framedGraph.addVertex("class:Attribute", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Attribute])
        entity1Attr.setName("parentKey")
        entity1Attr.setValue("parentValue")

        val entity2Attr = framedGraph.addVertex("class:Attribute", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Attribute])
        entity2Attr.setName("childKey")
        entity2Attr.setValue("childValue")

        val relationshipAttr1 = framedGraph.addVertex("class:Attribute", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Attribute])
        relationshipAttr1.setName("relationshipKey1")
        relationshipAttr1.setValue("relationshipValue1")
        val relationshipAttr2 = framedGraph.addVertex("class:Attribute", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Attribute])
        relationshipAttr2.setName("relationshipKey2")
        relationshipAttr2.setValue("relationshipValue2")

        val entity1 = framedGraph.addVertex("class:Entity", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Entity])
        entity1.setGuid(UUID.randomUUID().toString)
        entity1.setCreatedBy("frameParent")
        entity1.setCreatedDate(new Timestamp(0L))
        entity1.addAttribute(entity1Attr)

        val entity2 = framedGraph.addVertex("class:Entity", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Entity])
        entity2.setGuid(guid)
        entity2.setCreatedBy("frameChild")
        entity2.setCreatedDate(new Timestamp(0L))
        entity2.addAttribute(entity2Attr)

        val entity3 = framedGraph.addVertex("class:Entity", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Entity])
        entity3.setGuid(guid)
        entity3.setCreatedBy("frameGrandChild")
        entity3.setCreatedDate(new Timestamp(0L))
        entity3.addAttribute(entity2Attr)

        val relationship1 = framedGraph.addVertex("class:Relationship", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Relationship])
        relationship1.setGuid(UUID.randomUUID().toString)
        relationship1.setCreatedBy("frameRelationship1")
        relationship1.setCreatedDate(new Timestamp(0L))
        relationship1.addAttribute(relationshipAttr1)
        relationship1.setEntityType("TestRelation1")
        relationship1.setEntity1(entity1)
        relationship1.setEntity2(entity2)

        val relationship2 = framedGraph.addVertex("class:Relationship", classOf[org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames.Relationship])
        relationship2.setGuid(UUID.randomUUID().toString)
        relationship2.setCreatedBy("frameRelationship2")
        relationship2.setCreatedDate(new Timestamp(0L))
        relationship2.addAttribute(relationshipAttr2)
        relationship2.setEntityType("TestRelation2")
        relationship2.setEntity1(entity2)
        relationship2.setEntity2(entity3)

        framedGraph.commit()
      } finally {
        graph2.shutdown()
      }

      val graph: OrientGraph = new OrientGraphFactory(url, "admin", "admin").getTx
      try {
        val seq = graph.getVertices("Entity.guid", guid2).seq
        val head = seq.head
        val relationshipClass = graph.getRawGraph.getMetadata.getSchema.getClass("Relationship")
        println("head = " + head)
        println("seq.size = " + seq.size)
        println("head.edges = " + head.getEdges(Direction.BOTH).toSeq)
        println("head.getPropertyKeys = " + head.getPropertyKeys.toList)
        println("head.getProperty(entity1) = " + head.getProperty[Vertex]("entity1"))
        println("head.getProperty(entity1).getPropertyKeys = " + head.getProperty[Vertex]("entity1").getPropertyKeys.toList)
        /* if we (force?) sync the schema, these should be filled in
        println("getType(entity1) = " + relationshipClass.getProperty("entity1").getType)
        println("getLinkedType(entity1) = " + relationshipClass.getProperty("entity1").getLinkedType)
        println("getLinkedClass(entity1) = " + relationshipClass.getProperty("entity1").getLinkedClass)
        println("getType(attributes) = " + relationshipClass.getProperty("attributes").getType)
        println("getLinkedType(attributes) = " + relationshipClass.getProperty("attributes").getLinkedType)
        println("getLinkedClass(attributes) = " + relationshipClass.getProperty("attributes").getLinkedClass)
        */
      } finally {
        graph.shutdown()
      }
    }
  }
}
