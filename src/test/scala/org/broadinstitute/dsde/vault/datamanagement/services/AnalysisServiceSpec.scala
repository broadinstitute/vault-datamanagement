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
      val metadata = Map("ownerId" -> "user")
      var createdId: Option[String] = None

      // this test relies on adding relations to pre-existing ubams. Create those ubams first!
      val input = (for (x <- 1 to 3) yield DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, Map.empty), "AnalysisServiceSpec").id.get).toList

      "POST should store a new Analysis" in {
        assume(!input.isEmpty)

        Post(pathBase, Analysis(input, metadata, files = files)) ~> ingestRoute ~> check {
          val analysis = responseAs[Analysis]
          analysis.input.sorted should be(input.sorted)
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
          analysis.input.sorted should be(input.sorted)
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
        Post(pathBase, Analysis(input :+ "intentionallyBadForeignKey", metadata, files = files)) ~> sealRoute(ingestRoute) ~> check {
          status === BadRequest
        }
      }

      "POST of an Analysis object without files should store a new Analysis" in {
        assume(!input.isEmpty)

        val analysisIngest = Analysis(input, metadata)

        Post(pathBase, analysisIngest) ~> ingestRoute ~> check {
          val analysis = responseAs[Analysis]
          analysis.input should be(input)
          analysis.files should be(empty)
          analysis.metadata should be(metadata)
          analysis.id shouldNot be(empty)
        }
      }



    }
  }
}
