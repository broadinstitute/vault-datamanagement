package org.broadinstitute.dsde.vault.datamanagement.services

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.common.directives.VersioningDirectives._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.EntitySearchResult
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.json._
import spray.routing._

@Api(value = "/query", description = "Lookup Service", produces = "application/json")
trait LookupService extends HttpService {

  private final val ApiPrefix = "query"
  private final val ApiVersions = "v1"

  val routes = lookupEntityByTypeAttributeNameValueRoute

  @ApiOperation(value = "Lookup an entity by its type plus an attribute name + value.",
    nickname = "lookup_by_type_attribute",
    httpMethod = "GET",
    produces = "application/json",
    response = classOf[EntitySearchResult]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "version", required = true, dataType = "string", paramType = "path", value = "API version", allowableValues = ApiVersions),
    new ApiImplicitParam(name = "entityType", required = true, dataType = "string", paramType = "path", value = "entity type"),
    new ApiImplicitParam(name = "attributeName", required = true, dataType = "string", paramType = "path", value = "attribute name"),
    new ApiImplicitParam(name = "attributeValue", required = true, dataType = "string", paramType = "path", value = "attribute value")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful Request"),
    new ApiResponse(code = 404, message = "Vault Entity Not Found"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def lookupEntityByTypeAttributeNameValueRoute = {
    pathVersion(ApiPrefix, Segment / Segment / Segment) { (version, entityType, attributeName, attributeValue) =>
      get {
        rejectEmptyResponse {
          respondWithMediaType(`application/json`) {
            complete {
              version match {
                case _ =>
                  DataManagementController.lookupEntityByEndpointAttribute(
                    entityType, attributeName, attributeValue).map(_.toJson.prettyPrint)
              }
            }
          }
        }
      }
    }
  }

}
