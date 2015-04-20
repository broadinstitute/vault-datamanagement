package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.common.openam.OpenAMSession
import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.model.UBamCollection

class UBamCollectionServiceSpec extends DataManagementDatabaseFreeSpec with UBamCollectionService {

  def actorRefFactory = system

  val pathBase = "/collections"

  "UBamCollectionService" - {
    "when accessing the /collections path" - {
      val members = Array("Ubam_id_1", "Ubam_id_2", "Ubam_id_3")
      val metadata = Map("key1" -> "someKey", "key2" -> "otherKey", "key3" -> "anotherKey")

      "POST should store a new Collection" in {
        Post(pathBase, UBamCollection(members, metadata)) ~> OpenAMSession ~> ingestRoute ~> check {
          val collection = responseAs[UBamCollection]
          collection.metadata should be(metadata)
          collection.members should be(members)
        }
      }
    }
  }
}