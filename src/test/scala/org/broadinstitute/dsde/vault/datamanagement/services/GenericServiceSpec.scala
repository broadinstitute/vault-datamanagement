package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.model._
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

class GenericServiceSpec extends DataManagementDatabaseFreeSpec with GenericService   {

  "GenericService" - {

    val versions = Table(
      "version",
      None,
      Option(1)
    )

    forAll(versions) { (version: Option[Int]) =>
      val pathBase = "/entities" + v(version)

      s"when accessing the $pathBase path" - {
          val aValue = "val"+v(version)
          val uBAMAttrs = Map("ownerId" -> "me", "queryAttr" -> aValue)
          val bamBossId = "bamId"+v(version)
          val baiBossId = "baiId"+v(version)
          val bamAttrs = Map.empty[String,String]
          val baiAttrs = Map.empty[String,String]
          val bamRelAttrs = Map("name" -> "bam")
          val baiRelAttrs = Map("name" -> "bai")
          val ingest = GenericIngest(
              Some(List(GenericEntityIngest("unmappedBAM",None,uBAMAttrs),
                        GenericEntityIngest("file",Some(bamBossId),bamAttrs),
                        GenericEntityIngest("file",Some(baiBossId),baiAttrs))),
              Some(List(GenericRelationshipIngest("fileType","$0","$1",bamRelAttrs),
                        GenericRelationshipIngest("fileType","$0","$2",baiRelAttrs))))

        var guids: List[String] = List.empty[String]

        "POST should store a new uBAM generically" in {
          Post(s"$pathBase", ingest) ~> openAMSession ~> sealRoute(ingestRoute) ~> check {
            guids = responseAs[List[String]]
            guids should have length 3
          }
        }

        "findEntities should retrieve the ID of the bam file" in {
          Get(s"$pathBase",GenericEntityQuery("unmappedBAM",Seq(GenericAttributeSpec("queryAttr",aValue),GenericAttributeSpec("ownerId","me")),false)) ~>
            sealRoute(findEntitiesByTypeAndAttrRoute) ~> check {
            val files = responseAs[List[GenericEntity]]
            files should have length 1
            files(0).guid shouldBe guids(0)
            files(0).attrs shouldBe empty
          }
        }

        "findEntities should retrieve the ID and attributes of the bam file" in {
          Get(s"$pathBase",GenericEntityQuery("unmappedBAM",Seq(GenericAttributeSpec("queryAttr",aValue),GenericAttributeSpec("ownerId","me")),true)) ~>
                      sealRoute(findEntitiesByTypeAndAttrRoute) ~> check {
            val files = responseAs[List[GenericEntity]]
            files should have length 1
            files(0).guid shouldBe guids(0)
            files(0).attrs shouldBe Some(uBAMAttrs)
            files(0).attrs.get("queryAttr") shouldBe aValue
            files(0).attrs.get("ownerId") shouldBe "me"
          }
        }

        "findEntities with bogus attribute value should return NOT FOUND" in {
          Get(s"$pathBase",GenericEntityQuery("file",Seq(GenericAttributeSpec("path","foo")),false)) ~> sealRoute(findEntitiesByTypeAndAttrRoute) ~> check {
            val files = responseAs[List[GenericEntity]]
            files should have length 0
          }
        }

        "fetchEntity should retrieve the uBAM" in {
          Get(s"$pathBase/" + guids(0)) ~> sealRoute(fetchEntityRoute) ~> check {
            val uBAM = responseAs[GenericEntity]
            uBAM.guid shouldBe guids(0)
            uBAM.entityType shouldBe "unmappedBAM"
            uBAM.sysAttrs.bossID shouldBe empty
            uBAM.sysAttrs.createdBy shouldBe "TestAccount"
            uBAM.attrs shouldBe Some(uBAMAttrs)
          }
        }

        "fetchEntity on an unknown id should return a not found error" in {
          Get(s"$pathBase/unknown-id") ~> sealRoute(fetchEntityRoute) ~> check {
            status should be(NotFound)
          }
        }

        "findDownstream from the uBAM ought to return the bam and bai files" in {
          Get(s"$pathBase/"+guids(0)+"?down") ~> sealRoute(findDownstreamRoute) ~> check {
            val dRelEnts = responseAs[List[GenericRelEnt]]
            dRelEnts should have length 2
            val bamRelEnt = if ( dRelEnts(0).entity.guid == guids(1) ) dRelEnts(0) else dRelEnts(1)
            bamRelEnt.relationship.relationType shouldBe "fileType"
            bamRelEnt.relationship.attrs shouldBe Some(bamRelAttrs)
            bamRelEnt.entity.guid shouldBe(guids(1))
            bamRelEnt.entity.entityType shouldBe "file"
            bamRelEnt.entity.sysAttrs.bossID shouldBe Some(bamBossId)
            bamRelEnt.entity.sysAttrs.createdBy shouldBe "TestAccount"
            bamRelEnt.entity.attrs shouldBe Some(bamAttrs)
            val baiRelEnt = if ( dRelEnts(1).entity.guid == guids(2) ) dRelEnts(1) else dRelEnts(0)
            baiRelEnt.relationship.relationType shouldBe "fileType"
            baiRelEnt.relationship.attrs shouldBe Some(baiRelAttrs)
            baiRelEnt.entity.guid shouldBe(guids(2))
            baiRelEnt.entity.entityType shouldBe "file"
            baiRelEnt.entity.sysAttrs.bossID shouldBe Some(baiBossId)
            baiRelEnt.entity.sysAttrs.createdBy shouldBe "TestAccount"
            baiRelEnt.entity.attrs shouldBe Some(baiAttrs)
          }
        }

        "findUpstream from the bai ought to return the uBAM" in {
          Get(s"$pathBase/"+guids(1)+"?up") ~> sealRoute(findUpstreamRoute) ~> check {
            val uRelEnts = responseAs[List[GenericRelEnt]]
            uRelEnts should have length 1
            val uBAMRelEnt = uRelEnts(0)
            uBAMRelEnt.relationship.relationType shouldBe "fileType"
            uBAMRelEnt.relationship.attrs shouldBe Some(bamRelAttrs)
            uBAMRelEnt.entity.guid shouldBe(guids(0))
            uBAMRelEnt.entity.entityType shouldBe "unmappedBAM"
            uBAMRelEnt.entity.sysAttrs.bossID shouldBe empty
            uBAMRelEnt.entity.sysAttrs.createdBy shouldBe "TestAccount"
            uBAMRelEnt.entity.attrs shouldBe Some(uBAMAttrs)
          }
        }
      }
    }
  }

}