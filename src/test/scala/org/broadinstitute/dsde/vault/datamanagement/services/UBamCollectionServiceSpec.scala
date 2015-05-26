package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.{DataManagementDatabaseFreeSpec}
import org.broadinstitute.dsde.vault.datamanagement.controller.{EntityType, DataManagementController}
import org.broadinstitute.dsde.vault.datamanagement.model.{TermSearch, IndexResponse, UBamCollection, UnmappedBAM}
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import org.broadinstitute.dsde.vault.datamanagement.model.Properties._
import spray.httpx.SprayJsonSupport._

class UBamCollectionServiceSpec extends DataManagementDatabaseFreeSpec with UBamCollectionService with  IndexService{


  "UBamCollectionService" - {

    val versions = Table(
      "version",
      1
    )

    forAll(versions) { (version: Int) =>
      val pathBase = "/ubamcollections/v" + version
      val indexPathBase = "/admin/index/v" + version
      s"when accessing the $pathBase path" - {

        val metadata = Option(Map("key1" -> "someKey", "key2" -> "otherKey", "key3" -> "anotherKey"))

        val members = Option((
          for (x <- 1 to 3) yield
          DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, Map.empty), "UBamCollectionServiceSpec", includeProperties = true).id.get
          ).sorted.toSeq)

        var createdId: Option[String] = None
        var properties: Option[Map[String, String]] = None
        var isIndexed: Boolean = false

        "POST should store a new Collection" in {
         Post( s"$pathBase", UBamCollection(members, metadata)) ~> openAMSession ~> ingestRoute ~> check {
            val collection =  responseAs[UBamCollection]
            collection.metadata should be(metadata)
            collection.members should be(members)
            collection.properties shouldNot be(empty)
            collection.id shouldNot be(empty)
            collection.properties.get.get(CreatedBy) shouldNot be(empty)
            collection.properties.get.get(CreatedDate) shouldNot be(empty)
            collection.id shouldNot be(empty)
            createdId = collection.id
            properties = collection.properties
          }
        }

        "GET should retrieve the previously stored Collection" in {
          assume(createdId.isDefined)
          Get(s"$pathBase/" + createdId.get) ~> openAMSession ~> describeRoute ~> check {
            val collection = responseAs[UBamCollection]
            collection.metadata should be(metadata)
            collection.members.map(_.sorted) should be(members)
            collection.properties should be(properties)
            collection.id should be(createdId)
          }
        }

        "POST should index the previously stored Collections" in {
          assume(createdId.isDefined)
          Post(s"$indexPathBase/"+EntityType.UBAM_COLLECTION.databaseKey) ~> openAMSession ~> indexRoute ~> check {
            val indexResult = responseAs[IndexResponse]
            indexResult.messageResult should be("Index  successful")
            isIndexed = true
          }
        }
        "POST should get an error for invalid entity type" in {
         Post(s"$indexPathBase/Test") ~> openAMSession ~> indexRoute ~> check {
            val indexResult = responseAs[IndexResponse]
            indexResult.messageResult should be("Entity type does not exist")
          }
        }

        "POST should retrieve the Collections that matches the search criteria " in {
         assume(isIndexed == true)
          val term: TermSearch = new TermSearch("key1","someKey")
          val listTerms: List[TermSearch] =  List.apply(term)

          Post( s"$pathBase/search", listTerms) ~> openAMSession ~> searchRoute ~> check {
            val collections = responseAs[List[UBamCollection]]
            collections should not be empty
           }
        }
      }
    }
  }
}
