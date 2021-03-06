package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.{Analysis, UnmappedBAM}
import org.broadinstitute.dsde.vault.datamanagement.model.Properties._
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing.ExceptionHandler

class AnalysisServiceSpec extends DataManagementDatabaseFreeSpec with AnalysisService {
  "AnalysisService" - {

    val versions = Table(
      "version",
      None,
      Option(1),
      Option(2)
    )

    forAll(versions) { (version: Option[Int]) =>
      val pathBase = "/analyses" + v(version)

      s"when accessing the $pathBase path" - {

        val files = Option(Map.empty[String, String])
        val metadata = Option(Map.empty[String, String])
        var properties = Option(Map.empty[String, String])
        var createdId: Option[String] = None

        // this test relies on adding relations to pre-existing ubams. Create those ubams first!
        val input = Option((
          for (x <- 1 to 3) yield
          DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, Map.empty), "AnalysisServiceSpec", includeProperties = true).id.get
          ).sorted.toSeq)

        "POST should store a new Analysis" in {
          assume(input.nonEmpty)

          Post(pathBase, Analysis(input, metadata, files = files)) ~> openAMSession ~> ingestRoute ~> check {
            val analysis = responseAs[Analysis]
            analysis.input.map(_.sorted) should be(input)
            analysis.files should be(files)
            analysis.metadata should be(metadata)
            analysis.id shouldNot be(empty)
            createdId = analysis.id

            version match {
              case Some(x) if x > 1 =>
                analysis.properties shouldNot be(empty)
                analysis.properties.get.get(CreatedBy) shouldNot be(empty)
                analysis.properties.get.get(CreatedDate) shouldNot be(empty)
                properties = analysis.properties
              case _ => analysis.properties should be(empty)
            }
          }
        }

        "GET should retrieve the previously stored Analysis" in {
          assume(createdId.isDefined)

          Get(pathBase + "/" + createdId.get) ~> describeRoute ~> check {
            val analysis = responseAs[Analysis]
            analysis.input.map(_.sorted) should be(input)
            analysis.files should be(files)
            analysis.metadata should be(metadata)
            analysis.id should be(createdId)

            version match {
              case Some(x) if x > 1 => analysis.properties should be(properties)
              case _ => analysis.properties should be(empty)
            }
          }
        }

        "GET of an unknown id should return a not found error" in {
          Get(pathBase + "/unknown-id") ~> sealRoute(describeRoute) ~> check {
            status should be(NotFound)
          }
        }

        "POST with a bad input id should return a bad request error" in {
          val badInput = input.map(_ :+ "intentionallyBadForeignKey")
          // this test is expected to generate an integrity constraint exception
          implicit def myExceptionHandler: ExceptionHandler =
            ExceptionHandler {
              case e: java.sql.SQLException =>
                complete(InternalServerError, "intentionally handled error")
            }

          Post(pathBase, Analysis(badInput, metadata, files)) ~> openAMSession ~> sealRoute(ingestRoute) ~> check {
            status should be(InternalServerError)
            responseAs[String] should be("intentionally handled error")
          }
        }

        "POST of an Analysis object without files should store a new Analysis" in {
          assume(input.nonEmpty)

          val analysisIngest = Analysis(input, metadata)

          Post(pathBase, analysisIngest) ~> openAMSession ~> ingestRoute ~> check {
            val analysis = responseAs[Analysis]
            analysis.input.map(_.sorted) should be(input)
            analysis.files should be(empty)
            analysis.metadata should be(metadata)
            analysis.id shouldNot be(empty)

            version match {
              case Some(x) if x > 1 => {
                analysis.properties.get.get(CreatedBy) shouldNot be(empty)
                analysis.properties.get.get(CreatedDate) shouldNot be(empty)
                analysis.properties.get.get(ModifiedBy) should be(empty)
                analysis.properties.get.get(ModifiedDate) should be(empty)
              }
              case _ => analysis.properties should be(empty)
            }
          }
        }

        "POST of an Analysis object completion with files should update an Analysis" in {
          val analysisCreated = DataManagementController.createAnalysis(
            Analysis(input, metadata, files), "userCreate", includeProperties = true)

          val completedFiles = Option(Map(
            "vcf" -> "gcs://path/to/vcf",
            "bam" -> "gcs://path/to/bam",
            "bai" -> "gcs://path/to/bai",
            "adapter_metrics" -> "gcs://path/to/adapter_metrics",
            "alignment_summary_metrics" -> "gcs://path/to/alignment_summary_metrics"
          ))
          val completedMetadata = Option(Map(
            "newKey" -> "newValue"
          ))

          val analysisComplete = analysisCreated.copy(metadata = completedMetadata, files = completedFiles)

          Post(pathBase + "/" + analysisComplete.id.get + "/outputs", analysisComplete) ~> openAMSession ~> completeRoute ~> check {
            val analysis = responseAs[Analysis]
            analysis.input.map(_.sorted) should be(input)
            analysis.files should be(completedFiles)
            // NOTE: metadata should NOT currently be updated
            analysis.metadata should be(metadata)
            analysis.id shouldNot be(empty)

            version match {
              case Some(x) if x > 1 =>
                analysis.properties.get(CreatedBy) shouldNot be(empty)
                analysis.properties.get(CreatedDate) shouldNot be(empty)
                analysis.properties.get(ModifiedBy) shouldNot be(empty)
                analysis.properties.get(ModifiedDate) shouldNot be(empty)
              case _ => analysis.properties should be(empty)
            }
          }

          val entity = DataManagementController.getEntity(analysisComplete.id.get)
          entity shouldNot be(empty)
          entity.get.createdBy should be("userCreate")
          entity.get.modifiedBy shouldNot be(empty)
          entity.get.modifiedBy.get shouldNot be(empty)
        }

      }
    }
  }
}
