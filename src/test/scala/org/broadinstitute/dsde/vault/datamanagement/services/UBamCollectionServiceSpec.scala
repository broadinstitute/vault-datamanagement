package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.common.openam.OpenAMSession
import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.{UBamCollection, UnmappedBAM}
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.httpx.SprayJsonSupport._

class UBamCollectionServiceSpec extends DataManagementDatabaseFreeSpec with UBamCollectionService {

  def actorRefFactory = system
  val pathBase = "/collections"

  "UBamCollectionService" - {
    "when accessing the /collections path" - {

      val metadata = Option(Map("key1" -> "someKey", "key2" -> "otherKey", "key3" -> "anotherKey"))

      val members = Option((
        for (x <- 1 to 3) yield
        DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, Map.empty), "UBamCollectionServiceSpec").id.get
        ).sorted.toSeq)

      var createdId: Option[String] = None

      "POST should store a new Collection" in {
        Post( pathBase , UBamCollection(members, metadata)) ~> OpenAMSession ~> ingestRoute ~> check {
          val collection = responseAs[UBamCollection]
          collection.metadata should be(metadata)
          collection.members should be(members)
          createdId = collection.id
        }
      }
      "GET should retrieve the previously stored Collection" in {
        assume(createdId.isDefined)
        Get(pathBase+"/"+createdId.get) ~> OpenAMSession ~> describeRoute ~> check {
          val collection = responseAs[UBamCollection]
          collection.metadata should be(metadata)
          collection.members should be(members)
          collection.id should be(createdId)
        }
      }

    }
  }
}

