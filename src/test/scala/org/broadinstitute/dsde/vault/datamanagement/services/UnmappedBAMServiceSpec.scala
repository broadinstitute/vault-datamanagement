package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.model.UnmappedBAM
import org.broadinstitute.dsde.vault.datamanagement.model.Properties._
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

class UnmappedBAMServiceSpec extends DataManagementDatabaseFreeSpec with UnmappedBAMService {

  "UnmappedBAMService" - {

    val versions = Table(
      "version",
      None,
      Option(1),
      Option(2)
    )

    forAll(versions) { (version: Option[Int]) =>
      val pathBase = "/ubams" + v(version)

      s"when accessing the $pathBase path" - {
        val files = Map("bam" -> "/path/to/bam", "bai" -> "/path/to/bai")
        val metadata = Map("someKey" -> "someValue")
        var properties: Option[Map[String, String]] = None
        var createdId: Option[String] = None

        "POST should store a new unmapped BAM" in {
          Post(s"$pathBase", UnmappedBAM(files, metadata)) ~> openAMSession ~> ingestRoute ~> check {
            val unmappedBAM = responseAs[UnmappedBAM]
            unmappedBAM.files should be(files)
            unmappedBAM.metadata should be(metadata)
            unmappedBAM.id shouldNot be(empty)
            createdId = unmappedBAM.id

            version match {
              case Some(x) if x > 1 => {
                unmappedBAM.properties.get(CreatedBy) shouldNot be(empty)
                unmappedBAM.properties.get(CreatedDate) shouldNot be(empty)
                properties = unmappedBAM.properties
              }
              case _ => unmappedBAM.properties should be(empty)
            }
          }
        }

        "GET should retrieve the previously stored unmapped BAM" in {
          assume(createdId.isDefined)

          Get(s"$pathBase/" + createdId.get) ~> describeRoute ~> check {
            val unmappedBAM = responseAs[UnmappedBAM]
            unmappedBAM.files should be(files)
            unmappedBAM.metadata should be(metadata)
            unmappedBAM.id should be(createdId)

            version match {
              case Some(x) if x > 1 => {
                unmappedBAM.properties should be(properties)
                unmappedBAM.properties.get.get(CreatedBy) shouldNot be(empty)
                unmappedBAM.properties.get.get(CreatedDate) shouldNot be(empty)
              }
              case _ => unmappedBAM.properties should be(empty)
            }
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
