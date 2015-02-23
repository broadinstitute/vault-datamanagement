package org.broadinstitute.dsde.vault.datamanagement.services

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.domain.Attribute
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.json._
import spray.routing._
import spray.httpx.SprayJsonSupport._

@Api(value = "/helloattribute", description = "Hello Attribute Service", produces = "application/json")
trait HelloAttributeService extends HttpService {

  val routes = readRoute ~ createRoute

  @ApiOperation(value = "Add a new attribute", nickname = "addAttribute", httpMethod = "PUT", consumes = "application/json", produces = "application/json", response = classOf[Attribute])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "json of attribute", required = true, dataType = "org.broadinstitute.dsde.vault.datamanagement.domain.Attribute", paramType = "body")
  ))
  def createRoute = {
    put {
      path("helloattribute") {
        entity(as[Attribute]) { attribute =>
          respondWithMediaType(`application/json`) {
            complete {
              DataManagementController.createAttribute(attribute.name, attribute.value).toJson.prettyPrint
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Get an attribute", nickname = "getAttribute", httpMethod = "GET", produces = "application/json", response = classOf[Attribute])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "ID of attribute", required = true, dataType = "Int", paramType = "path")
  ))
  def readRoute = get {
    path("helloattribute" / IntNumber) { id =>
      respondWithMediaType(`application/json`) {
        complete {
          DataManagementController.getAttribute(id).toJson.prettyPrint
        }
      }
    }
  }

}
