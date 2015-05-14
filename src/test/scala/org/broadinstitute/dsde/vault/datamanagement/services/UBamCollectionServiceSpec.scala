package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.{UBamCollection, UnmappedBAM}
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import org.broadinstitute.dsde.vault.datamanagement.model.Properties._
import spray.httpx.SprayJsonSupport._

class UBamCollectionServiceSpec extends DataManagementDatabaseFreeSpec with UBamCollectionService {

  "UBamCollectionService" - {

    val versions = Table(
      "version",
      1
    )

    forAll(versions) { (version: Int) =>
      val pathBase = "/ubamcollections/v" + version

      s"when accessing the $pathBase path" - {

        val metadata = Option(Map("key1" -> "someKey", "key2" -> "otherKey", "key3" -> "anotherKey"))

        val members = Option((
          for (x <- 1 to 3) yield
          DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, Map.empty), "UBamCollectionServiceSpec", includeProperties = true).id.get
          ).sorted.toSeq)

        var createdId: Option[String] = None
        var properties: Option[Map[String, String]] = None

        "POST should store a new Collection" in {
          Post( s"$pathBase", UBamCollection(members, metadata)) ~> openAMSession ~> ingestRoute ~> check {
            val collection = responseAs[UBamCollection]
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
      }
    }
  }
}
