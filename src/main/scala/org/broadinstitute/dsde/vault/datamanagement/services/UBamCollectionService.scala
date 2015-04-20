package org.broadinstitute.dsde.vault.datamanagement.services

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.common.openam.OpenAMDirectives._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.{UBamCollection}
import spray.json._
import spray.http.MediaTypes._
import spray.routing.HttpService

@Api(value = "/collections", description = "uBAM Collection Service", produces = "application/json")
trait UBamCollectionService extends HttpService {

  val routes = ingestRoute

  @ApiOperation(value = "Creates an uBAM collection", nickname = "ubam_collection_ingest", httpMethod = "POST",
    produces = "application/json", consumes = "application/json", response = classOf[UBamCollection],
    notes = "Accepts a json packet as POST. Creates a Vault collection object with the supplied metadata.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", required = true, dataType = "org.broadinstitute.dsde.vault.datamanagement.model.UBamCollection", paramType = "path", value = "entity type")
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
}
