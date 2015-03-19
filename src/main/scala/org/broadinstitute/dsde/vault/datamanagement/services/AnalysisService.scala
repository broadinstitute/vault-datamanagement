package org.broadinstitute.dsde.vault.datamanagement.services

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.Analysis
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._

@Api(value = "/analyses", description = "Analysis Service", produces = "application/json")
trait AnalysisService extends HttpService {

  val routes = describeRoute ~ ingestRoute

  @ApiOperation(value = "Describes an Analysis's metadata and associated files.",
    nickname = "analysis",
    httpMethod = "GET",
    produces = "application/json",
    response = classOf[Analysis],
    notes = ""
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", required = true, dataType = "string", paramType = "path", value = "Analysis Vault ID")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful Request"),
    new ApiResponse(code = 404, message = "Vault ID Not Found"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def describeRoute = {
    path("analyses" / Segment) { id =>
      get {
        rejectEmptyResponse {
          respondWithMediaType(`application/json`) {
            complete {
              DataManagementController.getAnalysis(id).map(_.toJson.prettyPrint)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates Analysis objects", nickname = "analysis_ingest", httpMethod = "POST",
    produces = "application/json", consumes = "application/json", response = classOf[Analysis],
    notes = "Accepts a json packet as POST. Creates a Vault object with the supplied metadata.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", required = true, dataType = "org.broadinstitute.dsde.vault.datamanagement.domain.Analysis", paramType = "body", value = "Analysis to create")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def ingestRoute = {
    path("analyses") {
      post {
        entity(as[Analysis]) { analysis =>
          respondWithMediaType(`application/json`) {
            complete {
              val ownerId = analysis.metadata("ownerId")
              DataManagementController.createAnalysis(analysis, ownerId).toJson.prettyPrint
            }
          }
        }
      }
    }
  }

}
