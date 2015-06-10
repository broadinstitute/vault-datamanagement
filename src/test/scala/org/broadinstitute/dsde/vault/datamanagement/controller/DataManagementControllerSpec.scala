package org.broadinstitute.dsde.vault.datamanagement.controller

import java.util.UUID

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.model.GenericAttributeSpec
import org.broadinstitute.dsde.vault.datamanagement.model.GenericEntityIngest
import org.broadinstitute.dsde.vault.datamanagement.model.GenericIngest
import org.broadinstitute.dsde.vault.datamanagement.model.GenericEntityQuery
import org.broadinstitute.dsde.vault.datamanagement.model.GenericRelationshipIngest

class DataManagementControllerSpec extends DataManagementDatabaseFreeSpec {
  "DataManagementController" - {
    "should insert and retrieve an attribute" in {
      val da = DataManagementController.dataAccess
      val db = DataManagementController.database
      db withTransaction {
        implicit session => {
          val entityInsert = da.insertEntity("aType", "me")
          val parentInsert = da.insertEntity("bType", "parent")
          val parentOfEntityInsert = da.insertEntity("cType", "parentOfEntity")
          val relationInsert = da.insertRelation(
            parentOfEntityInsert.guid.get,
            parentInsert.guid.get,
            entityInsert.guid.get)
          val attrInsert = da.insertAttribute(entityInsert.guid.get, "testName", "testValue")
          val attrSelect = da.getAttribute(attrInsert.id.get)

          attrInsert.id shouldNot be(empty)
          attrSelect.id should be(attrInsert.id)
        }
      }
    }
    "should be able to ingest and query generically" in {
      val da = DataManagementController.dataAccess
      val db = DataManagementController.database
      val someValue = UUID.randomUUID().toString
      db withSession {
        implicit session => {
          val uBAMAttrs = Map("ownerId" -> "testUser", "queryAttr" -> someValue)
          val bamAttrs = Map.empty[String,String]
          val baiAttrs = Map.empty[String,String]
          val bamRelAttrs = Map("name" -> "bam")
          val baiRelAttrs = Map("name" -> "bai")
          val ingest = GenericIngest(
              Some(List(GenericEntityIngest("unmappedBAM",None,uBAMAttrs),
                        GenericEntityIngest("file",Some("bossIdOfBam"),bamAttrs),
                        GenericEntityIngest("file",Some("bossIdOfBai"),baiAttrs))),
              Some(List(GenericRelationshipIngest("fileType","$0","$1",bamRelAttrs),
                        GenericRelationshipIngest("fileType","$0","$2",baiRelAttrs))))

          // do some ingestion
          val guids = da.ingestStuff(ingest, "testUser")
          guids should have length 3

          // list the IDs of "unmappedBAM" entities having a "queryAttr" of someValue
          val files = da.findEntities(GenericEntityQuery("unmappedBAM",Seq(GenericAttributeSpec("queryAttr",someValue)),false,Option.empty))
          files should have length 1
          val uBAM = files(0)
          uBAM.guid shouldBe guids(0)
          uBAM.attrs shouldBe empty

          // check the head entity
          val uBAMOpt = da.fetchEntity(guids(0))
          uBAMOpt shouldNot be(empty)
          val uBAMEnt = uBAMOpt.get
          uBAMEnt.guid shouldBe guids(0)
          uBAMEnt.entityType shouldBe "unmappedBAM"
          uBAMEnt.sysAttrs.bossID shouldBe empty
          uBAMEnt.sysAttrs.createdBy shouldBe "testUser"
          uBAMEnt.attrs shouldBe Some(uBAMAttrs)

          // check the kids
          val dRelEnts = da.findDownstream(guids(0))
          dRelEnts should have length 2
          val bamRelEnt = if ( dRelEnts(0).entity.guid == guids(1) ) dRelEnts(0) else dRelEnts(1)
          bamRelEnt.relationship.relationType shouldBe "fileType"
          bamRelEnt.relationship.attrs shouldBe Some(bamRelAttrs)
          bamRelEnt.entity.guid shouldBe(guids(1))
          bamRelEnt.entity.entityType shouldBe "file"
          bamRelEnt.entity.sysAttrs.bossID shouldBe Some("bossIdOfBam")
          bamRelEnt.entity.sysAttrs.createdBy shouldBe "testUser"
          bamRelEnt.entity.attrs shouldBe Some(bamAttrs)
          val baiRelEnt = if ( dRelEnts(1).entity.guid == guids(2) ) dRelEnts(1) else dRelEnts(0)
          baiRelEnt.relationship.relationType shouldBe "fileType"
          baiRelEnt.relationship.attrs shouldBe Some(baiRelAttrs)
          baiRelEnt.entity.guid shouldBe(guids(2))
          baiRelEnt.entity.entityType shouldBe "file"
          baiRelEnt.entity.sysAttrs.bossID shouldBe Some("bossIdOfBai")
          baiRelEnt.entity.sysAttrs.createdBy shouldBe "testUser"
          baiRelEnt.entity.attrs shouldBe Some(baiAttrs)

          // check the parent
          val uRelEnts = da.findUpstream(guids(1))
          uRelEnts should have length 1
          val uBAMRelEnt = uRelEnts(0)
          uBAMRelEnt.relationship.relationType shouldBe "fileType"
          uBAMRelEnt.relationship.attrs shouldBe Some(bamRelAttrs)
          uBAMRelEnt.entity.guid shouldBe(guids(0))
          uBAMRelEnt.entity.entityType shouldBe "unmappedBAM"
          uBAMRelEnt.entity.sysAttrs.bossID shouldBe empty
          uBAMRelEnt.entity.sysAttrs.createdBy shouldBe "testUser"
          uBAMRelEnt.entity.attrs shouldBe Some(uBAMAttrs)
        }
      }
    }
    "should be able to ingest and query generically returning metadata" in {
      val da = DataManagementController.dataAccess
      val db = DataManagementController.database
      val someValue = UUID.randomUUID().toString
      db withSession {
        implicit session => {
          val uBAMAttrs = Map("ownerId" -> "testUser", "queryAttr" -> someValue)
          val bamAttrs = Map.empty[String,String]
          val baiAttrs = Map.empty[String,String]
          val bamRelAttrs = Map("name" -> "bam")
          val baiRelAttrs = Map("name" -> "bai")
          val ingest = GenericIngest(
            Some(List(GenericEntityIngest("unmappedBAM",None,uBAMAttrs),
              GenericEntityIngest("file",Some("bossIdOfBam"),bamAttrs),
              GenericEntityIngest("file",Some("bossIdOfBai"),baiAttrs))),
            Some(List(GenericRelationshipIngest("fileType","$0","$1",bamRelAttrs),
              GenericRelationshipIngest("fileType","$0","$2",baiRelAttrs))))

          // do some ingestion
          val guids = da.ingestStuff(ingest, "testUser")
          guids should have length 3

          // list the IDs of "unmappedBAM" entities having a "queryAttr" of someValue
          val files = da.findEntities(GenericEntityQuery("unmappedBAM",Seq(GenericAttributeSpec("queryAttr",someValue)),true,Option.empty))
          files should have length 1
          val uBAM = files(0)
          uBAM.entityType shouldBe "unmappedBAM"
          uBAM.guid shouldBe guids(0)
          uBAM.attrs.get("ownerId") shouldBe "testUser"
          uBAM.attrs.get("queryAttr") shouldBe someValue
        }
      }
    }
  }
}
