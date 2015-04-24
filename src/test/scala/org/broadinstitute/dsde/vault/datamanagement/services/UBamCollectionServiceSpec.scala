package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.common.openam.OpenAMSession
import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.{UBamCollection, UnmappedBAM}
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.httpx.SprayJsonSupport._

class UBamCollectionServiceSpec extends DataManagementDatabaseFreeSpec with UBamCollectionService {

  def actorRefFactory = system

  "UBamCollectionService" - {
    "when accessing the /collections path" - {
      //val members = Some(List("Ubam_id_1", "Ubam_id_2", "Ubam_id_3").toSeq)
      val metadata = Map("key1" -> "someKey", "key2" -> "otherKey", "key3" -> "anotherKey")

      val members = Option((
        for (x <- 1 to 3) yield
        DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, Map.empty), "UBamCollectionServiceSpec").id.get
        ).sorted.toSeq)

      "POST should store a new Collection" in {
        Post( "/collections", UBamCollection(members, metadata)) ~> OpenAMSession ~> ingestRoute ~> check {
          val collection = responseAs[UBamCollection]
          collection.metadata should be(metadata)
          collection.members should be(members)
        }
      }
    }
  }


/*  override def afterAll(): Unit = {
  }*/
}