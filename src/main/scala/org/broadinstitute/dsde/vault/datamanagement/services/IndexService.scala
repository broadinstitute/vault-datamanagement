package org.broadinstitute.dsde.vault.datamanagement.services

import javax.ws.rs.{POST, Path}

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.json._
import spray.routing._


@Api(value = "/admin")
trait IndexService extends HttpService {

  val adminRoutes = indexRoute
  private final val ApiVersions = "v1,v2"

 @POST
 @Path("/index/{version}/{entityType}")
 @ApiOperation(value = "Index all existing collection metadata.", nickname = "index", httpMethod = "POST",
   produces = "application/json", consumes = "application/json", notes = "Accepts an Entity Type as POST (unmappedBAM, analysis, uBAMCollection). Index the selected Entity and its metadata. ")
 @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "version", required = true, dataType = "string", paramType = "path", value = "API version", allowableValues = ApiVersions),
   new ApiImplicitParam(name = "entityType", required = true, dataType = "string", paramType = "path", value = "Entity Type")
 ))
 @ApiResponses(Array(
   new ApiResponse(code = 200, message = "Successful"),
   new ApiResponse(code = 400, message = "Bad Request"),
   new ApiResponse(code = 500, message = "Vault Internal Error")
 ))
  def indexRoute = {
   path("admin" / "index" /"v" ~ IntNumber / Segment) { (version,entityType) => {
     post {
       rejectEmptyResponse {
         respondWithMediaType(`application/json`) {
           complete {
             DataManagementController.index(entityType, Option(version)).toJson.prettyPrint
           }
         }
       }
     }
    }
   }
 }

}
