package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.common.openam.OpenAMSession
import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.model.UnmappedBAM
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

class UnmappedBAMServiceSpec extends DataManagementDatabaseFreeSpec with UnmappedBAMService {

  def actorRefFactory = system

  "UnmappedBAMService" - {
    "when accessing the /ubams path" - {
      val files = Map("bam" -> "/path/to/bam", "bai" -> "/path/to/bai")
      val metadata = Map("ownerId" -> "user")
      var createdId: Option[String] = None

      "POST should store a new unmapped BAM" in {
        Post("/ubams", UnmappedBAM(files, metadata)) ~> OpenAMSession ~> ingestRoute ~> check {
          val unmappedBAM = responseAs[UnmappedBAM]
          unmappedBAM.files should be(files)
          unmappedBAM.metadata should be(metadata)
          unmappedBAM.id shouldNot be(empty)
          createdId = unmappedBAM.id
        }
      }

      "GET should retrieve the previously stored unmapped BAM" in {
        assume(createdId.isDefined)

        Get("/ubams/" + createdId.get) ~> describeRoute ~> check {
          val unmappedBAM = responseAs[UnmappedBAM]
          unmappedBAM.files should be(files)
          unmappedBAM.metadata should be(metadata)
          unmappedBAM.id should be(createdId)
        }
      }

      "GET of an unknown id should return a not found error" in {
        Get("/ubams/unknown-id") ~> sealRoute(describeRoute) ~> check {
          status === NotFound
        }
      }
    }
  }
}
