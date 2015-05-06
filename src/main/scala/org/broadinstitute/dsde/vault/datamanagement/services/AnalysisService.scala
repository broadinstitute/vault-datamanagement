package org.broadinstitute.dsde.vault.datamanagement.services

import javax.ws.rs.Path

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.common.directives.OpenAMDirectives._
import org.broadinstitute.dsde.vault.common.directives.VersioningDirectives._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.Analysis
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._

@Api(value = "/analyses", description = "Analysis Service", produces = "application/json")
trait AnalysisService extends HttpService {

  private implicit val ec = actorRefFactory.dispatcher

  private final val ApiPrefix = "analyses"
  private final val ApiVersions = "v1"

  val routes = describeRoute ~ ingestRoute ~ completeRoute

  @ApiOperation(value = "Describes an Analysis's metadata and associated files.",
    nickname = "analysis",
    httpMethod = "GET",
    produces = "application/json",
    response = classOf[Analysis],
    notes = ""
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "version", required = true, dataType = "string", paramType = "path", value = "API version", allowableValues = ApiVersions),
    new ApiImplicitParam(name = "id", required = true, dataType = "string", paramType = "path", value = "Analysis Vault ID")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful Request"),
    new ApiResponse(code = 404, message = "Vault ID Not Found"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def describeRoute = {
    pathVersion(ApiPrefix, Segment) { (version, id) =>
      get {
        rejectEmptyResponse {
          respondWithMediaType(`application/json`) {
            complete {
              version match {
                case _ =>
                  DataManagementController.getAnalysis(id).map(_.toJson.prettyPrint)
              }
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
    new ApiImplicitParam(name = "version", required = true, dataType = "string", paramType = "path", value = "API version", allowableValues = ApiVersions),
    new ApiImplicitParam(name = "body", required = true, dataType = "org.broadinstitute.dsde.vault.datamanagement.model.Analysis", paramType = "body", value = "Analysis to create")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def ingestRoute = {
    pathVersion(ApiPrefix) { version =>
      post {
        commonNameFromCookie() { commonName =>
          entity(as[Analysis]) { analysis =>
            respondWithMediaType(`application/json`) {
              complete {
                version match {
                  case _ =>
                    DataManagementController.createAnalysis(analysis, commonName).toJson.prettyPrint
                }
              }
            }
          }
        }
      }
    }
  }

  @Path("/{id}/outputs")
  @ApiOperation(value = "Complete Analysis", nickname = "analysis_complete", httpMethod = "POST",
    produces = "application/json", consumes = "application/json", response = classOf[Analysis],
    notes = "Accepts a json packet as POST. Updates a Vault object with the supplied files.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "version", required = true, dataType = "string", paramType = "path", value = "API version", allowableValues = ApiVersions),
    new ApiImplicitParam(name = "id", required = true, dataType = "string", paramType = "path", value = "Analysis Vault ID"),
    new ApiImplicitParam(name = "body", required = true, dataType = "org.broadinstitute.dsde.vault.datamanagement.model.Analysis", paramType = "body", value = "Analysis to update")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def completeRoute = {
    pathVersion(ApiPrefix, Segment / "outputs") { (version, id) =>
      post {
        commonNameFromCookie() { commonName =>
          entity(as[Analysis]) { analysis =>
            respondWithMediaType(`application/json`) {
              complete {
                version match {
                  case _ =>
                    DataManagementController.completeAnalysis(id, analysis.files.get, commonName).toJson.prettyPrint
                }
              }
            }
          }
        }
      }
    }
  }

}
