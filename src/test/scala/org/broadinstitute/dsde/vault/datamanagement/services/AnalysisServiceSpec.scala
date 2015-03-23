package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.{Analysis, UnmappedBAM}
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

class AnalysisServiceSpec extends DataManagementDatabaseFreeSpec with AnalysisService {

  def actorRefFactory = system

  val pathBase = "/analyses"

  "AnalysisService" - {
    "when accessing the /analyses path" - {
      val files = Option(Map[String, String]())
      val metadata = Option(Map("ownerId" -> "user"))
      var createdId: Option[String] = None

      // this test relies on adding relations to pre-existing ubams. Create those ubams first!
      val input = Option((
        for (x <- 1 to 3) yield
          DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, Map.empty), "AnalysisServiceSpec").id.get
        ).sorted.toSeq)

      "POST should store a new Analysis" in {
        assume(input.nonEmpty)

        Post(pathBase, Analysis(input, metadata, files = files)) ~> ingestRoute ~> check {
          val analysis = responseAs[Analysis]
          analysis.input.map(_.sorted) should be(input)
          analysis.files should be(files)
          analysis.metadata should be(metadata)
          analysis.id shouldNot be(empty)
          createdId = analysis.id
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
        }
      }

      "GET of an unknown id should return a not found error" in {
        Get(pathBase + "/unknown-id") ~> sealRoute(describeRoute) ~> check {
          status === NotFound
        }
      }

      "POST with a bad input id should return a bad request error" in {
        val badInput = input.map(_ :+ "intentionallyBadForeignKey")
          Post(pathBase, Analysis(badInput, metadata, files)) ~> sealRoute(ingestRoute) ~> check {
            status === BadRequest
        }
      }

      "POST of an Analysis object without files should store a new Analysis" in {
        assume(input.nonEmpty)

        val analysisIngest = Analysis(input, metadata)

        Post(pathBase, analysisIngest) ~> ingestRoute ~> check {
          val analysis = responseAs[Analysis]
          analysis.input.map(_.sorted) should be(input)
          analysis.files should be(empty)
          analysis.metadata should be(metadata)
          analysis.id shouldNot be(empty)
        }
      }

      "POST of an Analysis object completion with files should update an Analysis" in {
        val analysisCreated = DataManagementController.createAnalysis(
          Analysis(input, metadata, files), "userCreate")

        val completedFiles = Option(Map(
          "vcf" -> "gcs://path/to/vcf",
          "bam" -> "gcs://path/to/bam",
          "bai" -> "gcs://path/to/bai",
          "adapter_metrics" -> "gcs://path/to/adapter_metrics",
          "alignment_summary_metrics" -> "gcs://path/to/alignment_summary_metrics"
        ))
        val completedMedatada = Option(Map(
          "ownerId" -> "userUpdate"
        ))

        val analysisComplete = analysisCreated.copy(metadata = completedMedatada, files = completedFiles)

        Post(pathBase + "/" + analysisComplete.id.get + "/outputs", analysisComplete) ~> completeRoute ~> check {
          val analysis = responseAs[Analysis]
          analysis.input.map(_.sorted) should be(input)
          analysis.files should be(completedFiles)
          // NOTE: for now, ownerId does NOT get updated, so this should be the metadata as passed in.
          analysis.metadata should be(metadata)
          analysis.id shouldNot be(empty)
        }

        val entity = DataManagementController.getEntity(analysisComplete.id.get)
        entity shouldNot be(empty)
        entity.get.createdBy should be("userCreate")
        entity.get.modifiedBy should be(Option("userUpdate"))
      }

    }
  }
}
