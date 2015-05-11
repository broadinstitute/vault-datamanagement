package org.broadinstitute.dsde.vault.datamanagement.services

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.common.directives.OpenAMDirectives._
import org.broadinstitute.dsde.vault.common.directives.VersioningDirectives._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.UnmappedBAM
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._

@Api(value = "/ubams", description = "uBAM Service", produces = "application/json")
trait UnmappedBAMService extends HttpService {

  private implicit val ec = actorRefFactory.dispatcher

  private final val ApiPrefix = "ubams"
  private final val ApiVersions = "v1,v2"

  val routes = describeRoute ~ ingestRoute

  @ApiOperation(value = "Describes a uBAM's metadata and associated files.",
    nickname = "ubam_describe",
    httpMethod = "GET",
    produces = "application/json",
    response = classOf[UnmappedBAM],
    notes = "Supports arbitrary metadata keys, but this is not represented well in Swagger (see the 'additionalMetadata' note below)"
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "version", required = true, dataType = "string", paramType = "path", value = "API version", allowableValues = ApiVersions),
    new ApiImplicitParam(name = "id", required = true, dataType = "string", paramType = "path", value = "uBAM Vault ID")
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
              DataManagementController.getUnmappedBAM(id, version).map(_.toJson.prettyPrint)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates uBAM objects", nickname = "ubam_ingest", httpMethod = "POST",
    produces = "application/json", consumes = "application/json", response = classOf[UnmappedBAM],
    notes = "Accepts a json packet as POST. Creates a Vault object with the supplied metadata.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "version", required = true, dataType = "string", paramType = "path", value = "API version", allowableValues = ApiVersions),
    new ApiImplicitParam(name = "body", required = true, dataType = "org.broadinstitute.dsde.vault.datamanagement.model.UnmappedBAM", paramType = "body", value = "uBAM to create")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def ingestRoute = {
    pathVersion(ApiPrefix) { version =>
      post {
        commonNameFromCookie() { commonName =>
          entity(as[UnmappedBAM]) { unmappedBAM =>
            respondWithMediaType(`application/json`) {
              complete {
                DataManagementController.createUnmappedBAM(unmappedBAM, commonName, version).toJson.prettyPrint
              }
            }
          }
        }
      }
    }
  }
}
