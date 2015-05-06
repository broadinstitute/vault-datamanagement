package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.model.UnmappedBAM
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

class UnmappedBAMServiceSpec extends DataManagementDatabaseFreeSpec with UnmappedBAMService {

  "UnmappedBAMService" - {

    val versions = Table(
      "version",
      None,
      Some(1)
    )

    forAll(versions) { (version: Option[Int]) =>
      val pathBase = "/ubams" + v(version)

      s"when accessing the $pathBase path" - {
        val files = Map("bam" -> "/path/to/bam", "bai" -> "/path/to/bai")
        val metadata = Map("someKey" -> "someValue")
        var createdId: Option[String] = None

        "POST should store a new unmapped BAM" in {
          Post(s"$pathBase", UnmappedBAM(files, metadata)) ~> openAMSession ~> ingestRoute ~> check {
            val unmappedBAM = responseAs[UnmappedBAM]
            unmappedBAM.files should be(files)
            unmappedBAM.metadata should be(metadata)
            unmappedBAM.id shouldNot be(empty)
            createdId = unmappedBAM.id
          }
        }

        "GET should retrieve the previously stored unmapped BAM" in {
          assume(createdId.isDefined)

          Get(s"$pathBase/" + createdId.get) ~> describeRoute ~> check {
            val unmappedBAM = responseAs[UnmappedBAM]
            unmappedBAM.files should be(files)
            unmappedBAM.metadata should be(metadata)
            unmappedBAM.id should be(createdId)
          }
        }

        "GET of an unknown id should return a not found error" in {
          Get(s"$pathBase/unknown-id") ~> sealRoute(describeRoute) ~> check {
            status should be(NotFound)
          }
        }
      }

    }
  }
}
