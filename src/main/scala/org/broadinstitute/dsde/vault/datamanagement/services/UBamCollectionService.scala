package org.broadinstitute.dsde.vault.datamanagement.services

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.common.openam.OpenAMDirectives._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.{UBamCollection}
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._

@Api(value = "/collections", description = "uBAM Collection Service", produces = "application/json")
trait UBamCollectionService extends HttpService {

  val routes = ingestRoute ~ describeRoute

  @ApiOperation(value = "Creates an uBAM collection", nickname = "ubam_collection_ingest", httpMethod = "POST",
    produces = "application/json", consumes = "application/json", response = classOf[UBamCollection],
    notes = "Accepts a json packet as POST. Creates a Vault collection object with the supplied ubam ids and the supplied metadata.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", required = true, dataType = "org.broadinstitute.dsde.vault.datamanagement.model.UBamCollection", paramType = "body", value = "Collection to create")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def ingestRoute = {
    path("collections") {
      post {
        commonNameFromCookie { commonName =>
          entity(as[UBamCollection]) { uBAMCollection =>
            respondWithMediaType(`application/json`) {
              complete {
                DataManagementController.createUBAMCollection(uBAMCollection, commonName).toJson.prettyPrint
              }
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Gets an Ubams Collection",
    nickname = "ubam_get_collection",
    httpMethod = "GET",
    produces = "application/json",
    response = classOf[UBamCollection],
    responseContainer = "List",
    notes = "Gets Vault collection object with the supplied UbamCollection id"
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", required = true, dataType = "string", paramType = "path", value = "uBAM Vault ID")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful Request"),
    new ApiResponse(code = 404, message = "Vault ID Not Found"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def describeRoute = {
    path("collections" / Segment) { id =>
      get {
        rejectEmptyResponse {
          respondWithMediaType(`application/json`) {
            complete {
              DataManagementController.getUBAMCollection(id).map(_.toJson.prettyPrint)
            }
          }
        }
      }
    }
  }


}
