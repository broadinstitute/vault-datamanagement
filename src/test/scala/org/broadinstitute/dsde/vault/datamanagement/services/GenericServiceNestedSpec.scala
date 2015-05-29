package org.broadinstitute.dsde.vault.datamanagement.services

import java.util.concurrent.TimeUnit

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.model._
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.duration.FiniteDuration

class GenericServiceNestedSpec extends DataManagementDatabaseFreeSpec with GenericService   {

  implicit val routeTestTimeout = RouteTestTimeout(new FiniteDuration(5, TimeUnit.SECONDS))

  "GenericService" - {

    val versions = Table(
      "version",
      None,
      Option(1)
    )

    forAll(versions) { (version: Option[Int]) =>
      val pathBase = "/entities" + v(version)

      /*
       * This models a root sample with multiple levels of child samples
       */
      s"when accessing the $pathBase path" - {
        // Root sample
        val aValue = java.util.UUID.randomUUID().toString
        val sampleAttrs = Map("ownerId" -> "me", "id" -> aValue, "container" -> "freezer")

        val aliquot1Attrs = Map("id" -> "aliquot1", "container" -> "tube")
        val aliquot1RelAttrs = Map("name" -> "aliquot")

        val aliquot2Attrs = Map("id" -> "aliquot2", "container" -> "tube")
        val aliquot2RelAttrs = Map("name" -> "aliquot")

        // Child samples for aliquot1
        val aliquot3Attrs = Map("id" -> "aliquot3", "container" -> "plate")
        val aliquot3RelAttrs = Map("name" -> "aliquot")

        val aliquot4Attrs = Map("id" -> "aliquot4", "container" -> "plate")
        val aliquot4RelAttrs = Map("name" -> "aliquot")

        // Child sample for aliquot2
        val aliquot5Attrs = Map("id" -> "aliquot5", "container" -> "plate")
        val aliquot5RelAttrs = Map("name" -> "aliquot")

        // Child sample for aliquot3, which is a child of aliquot1
        val aliquot6Attrs = Map("id" -> "aliquot6", "container" -> "plate")
        val aliquot6RelAttrs = Map("name" -> "aliquot")

        // Child sample for aliquot6, which is a grandchild of aliquot1
        val aliquot7Attrs = Map("id" -> "aliquot7", "container" -> "plate")
        val aliquot7RelAttrs = Map("name" -> "aliquot")

        val ingest = GenericIngest(
            Some(List(GenericEntityIngest("rootSample",None,sampleAttrs),
                      GenericEntityIngest("sample",None,aliquot1Attrs),
                      GenericEntityIngest("sample",None,aliquot2Attrs),
                      GenericEntityIngest("sample",None,aliquot3Attrs),
                      GenericEntityIngest("sample",None,aliquot4Attrs),
                      GenericEntityIngest("sample",None,aliquot5Attrs),
                      GenericEntityIngest("sample",None,aliquot6Attrs),
                      GenericEntityIngest("sample",None,aliquot7Attrs)
            )),
            Some(List(GenericRelationshipIngest("aliquot","$0","$1",aliquot1RelAttrs),
                      GenericRelationshipIngest("aliquot","$0","$2",aliquot2RelAttrs),
                      GenericRelationshipIngest("aliquot","$1","$3",aliquot3RelAttrs),
                      GenericRelationshipIngest("aliquot","$1","$4",aliquot4RelAttrs),
                      GenericRelationshipIngest("aliquot","$2","$5",aliquot5RelAttrs),
                      GenericRelationshipIngest("aliquot","$3","$6",aliquot6RelAttrs),
                      GenericRelationshipIngest("aliquot","$6","$7",aliquot7RelAttrs)
            )))

        var guids: List[String] = List.empty[String]

        "POST should store a new sample generically" in {
          Post(s"$pathBase", ingest) ~> openAMSession ~> sealRoute(routes) ~> check {
            guids = responseAs[List[String]]
            guids should have length 8
          }
        }

        val findEntityPath = s"$pathBase/search"
        "findEntities should retrieve the children of a root sample" in {
          Post(findEntityPath,GenericEntityQuery("rootSample", Seq(GenericAttributeSpec("id", aValue), GenericAttributeSpec("ownerId","me")),false, Option(1))) ~>
            sealRoute(routes) ~> check {
            val roots = responseAs[List[GenericEntity]]
            roots should have length 1
            val root = roots.head
            root.relEnts.get should have length 2

            // The depth parameter should have limited further relEnts from being populated
            root.relEnts.get.foreach {
              relEnt: GenericRelEnt =>
                relEnt.entity.relEnts should be (Option.empty)
            }
          }
        }

        "findEntities should retrieve the grand-children of a root sample" in {
          Post(findEntityPath,GenericEntityQuery("rootSample", Seq(GenericAttributeSpec("id", aValue), GenericAttributeSpec("ownerId","me")),false, Option(2))) ~>
            sealRoute(routes) ~> check {
            val roots = responseAs[List[GenericEntity]]
            roots should have length 1
            val root = roots.head
            root.relEnts.get should have length 2
            val aliquot1 = root.relEnts.get.head.entity
            aliquot1.relEnts.get should have length 2
            val aliquot2 = root.relEnts.get(1).entity
            aliquot2.relEnts.get should have length 1

            // The depth parameter should limit further relEnts from being populated
            aliquot1.relEnts.get.foreach {
              relEnt: GenericRelEnt =>
                relEnt.entity.relEnts should be (Option.empty)
            }
            aliquot2.relEnts.get.foreach {
              relEnt: GenericRelEnt =>
                relEnt.entity.relEnts should be (Option.empty)
            }
          }
        }

        "findEntities should retrieve the great grand-children of a root sample" in {
          Post(findEntityPath,GenericEntityQuery("rootSample", Seq(GenericAttributeSpec("id", aValue), GenericAttributeSpec("ownerId","me")),false, Option(3))) ~>
            sealRoute(routes) ~> check {
            val roots = responseAs[List[GenericEntity]]
            roots should have length 1
            val root = roots.head
            root.relEnts.get should have length 2
            val aliquot1 = root.relEnts.get.head.entity
            aliquot1.relEnts.get should have length 2

            val aliquot3 = aliquot1.relEnts.get.head.entity
            aliquot3.relEnts.get should have length 1

            // The depth parameter should limit further relEnts from being populated
            val aliquot6 = aliquot3.relEnts.get.head.entity
            aliquot6.relEnts should be (Option.empty)
          }
        }

        "findEntities should retrieve the great great grand-children of a root sample" in {
          Post(findEntityPath,GenericEntityQuery("rootSample", Seq(GenericAttributeSpec("id", aValue), GenericAttributeSpec("ownerId","me")),false, Option(4))) ~>
            sealRoute(routes) ~> check {
            val roots = responseAs[List[GenericEntity]]
            roots should have length 1
            val root = roots.head
            root.relEnts.get should have length 2
            val aliquot1 = root.relEnts.get.head.entity
            aliquot1.relEnts.get should have length 2

            val aliquot3 = aliquot1.relEnts.get.head.entity
            aliquot3.relEnts.get should have length 1

            val aliquot6 = aliquot3.relEnts.get.head.entity
            aliquot6.relEnts.get should have length 1

            // The depth parameter should limit further relEnts from being populated
            val aliquot7 = aliquot6.relEnts.get.head.entity
            aliquot7.relEnts should be (Option.empty)
          }
        }

        "findEntities should limit the depth retrieved" in {
          Post(findEntityPath,GenericEntityQuery("rootSample", Seq(GenericAttributeSpec("id", aValue), GenericAttributeSpec("ownerId","me")),false, Option(10))) ~>
            sealRoute(routes) ~> check {
            val roots = responseAs[List[GenericEntity]]
            val aliquot1 = roots.head.relEnts.get.head.entity
            val aliquot3 = aliquot1.relEnts.get.head.entity
            val aliquot6 = aliquot3.relEnts.get.head.entity
            val aliquot7 = aliquot6.relEnts.get.head.entity
            // The depth parameter should limit further relEnts from being populated
            aliquot7.relEnts should be (Option.empty)
          }
        }

      }
    }
  }

}
