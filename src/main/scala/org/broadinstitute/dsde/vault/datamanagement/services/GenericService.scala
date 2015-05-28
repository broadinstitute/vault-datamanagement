package org.broadinstitute.dsde.vault.datamanagement.services

import javax.ws.rs.Path

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.common.directives.OpenAMDirectives._
import org.broadinstitute.dsde.vault.common.directives.VersioningDirectives._
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model.GenericEntity
import org.broadinstitute.dsde.vault.datamanagement.model.GenericIngest
import org.broadinstitute.dsde.vault.datamanagement.model.GenericEntityQuery
import org.broadinstitute.dsde.vault.datamanagement.model.GenericRelEnt
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._

@Api(value="/entities", description="generic entity service", produces="application/json")
trait GenericService extends HttpService {

  private implicit val ec = actorRefFactory.dispatcher
  private final val ApiPrefix = "entities"
  private final val ApiVersions = "v1"
  private final val DefaultVersion = 1

  val routes = ingestRoute ~ findEntitiesByTypeAndAttrRoute ~ findUpstreamRoute ~ findDownstreamRoute ~ fetchEntityRoute

  @ApiOperation(
    value="store some entities and relationships",
    nickname="genericIngest",
    httpMethod="POST",
    response=classOf[String],
    responseContainer="List",
    notes="response is a list of vault IDs of the new entities")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name="version", required=true, dataType="string", paramType="path", value="API version", allowableValues=ApiVersions),
    new ApiImplicitParam(name="body", required=true, dataType="org.broadinstitute.dsde.vault.datamanagement.model.GenericIngest", paramType="body", value="entities and relations to create")))
  @ApiResponses(Array(
    new ApiResponse(code=200, message="Successful"),
    new ApiResponse(code=400, message="Bad Request"),
    new ApiResponse(code=500, message="Internal Error")))
  def ingestRoute = {
    pathVersion(ApiPrefix,DefaultVersion) { version =>
      post {
        commonNameFromCookie() { commonName =>
          entity(as[GenericIngest]) { ingest =>
            respondWithMediaType(`application/json`) {
              complete {
                DataManagementController.ingestStuff(ingest,commonName).toJson.prettyPrint
              }
            }
          }
        }
      }
    }
  }

  @Path("/{version}/search")
  @ApiOperation(
    value="find IDs of entities of a specified type having a specified metadata attribute value",
    nickname="findEntitiesByTypeAndAttr",
    httpMethod="POST",
    response=classOf[GenericEntity],
    responseContainer="List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name="version", required=true, dataType="string", paramType="path", value="API version", allowableValues=ApiVersions),
    new ApiImplicitParam(name="body", required=true, dataType="org.broadinstitute.dsde.vault.datamanagement.model.GenericEntityQuery", paramType="body", value="entities to find")))
  @ApiResponses(Array(
    new ApiResponse(code=200, message="Successful"),
    new ApiResponse(code=400, message="Bad Request"),
    new ApiResponse(code=404, message="Not Found"),
    new ApiResponse(code=500, message="Internal Error")))
  def findEntitiesByTypeAndAttrRoute = {
    pathVersion(ApiPrefix, DefaultVersion, "search") { version =>
      post {
        entity(as[GenericEntityQuery]) { query =>
          respondWithMediaType(`application/json`) {
            complete {
              DataManagementController.findEntitiesByTypeAndAttr(query).toJson.prettyPrint
            }
          }
        }
      }
    }
  }

  @ApiOperation(
    value="get data for a particular entity",
    nickname="fetchEntity",
    httpMethod="GET",
    response=classOf[GenericEntity])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name="version", required=true, dataType="string", paramType="path", value="API version", allowableValues=ApiVersions),
    new ApiImplicitParam(name="id", required=true, dataType="string", paramType="path", value="vault ID")))
  @ApiResponses(Array(
    new ApiResponse(code=200, message="Successful"),
    new ApiResponse(code=404, message="Not Found"),
    new ApiResponse(code=500, message="Internal Error")))
  def fetchEntityRoute = {
    pathVersion(ApiPrefix,DefaultVersion,Segment) { (version, guid) =>
      get {
        respondWithMediaType(`application/json`) {
          complete {
            DataManagementController.fetchEntity(guid) map {_.toJson.prettyPrint}
          }
        }
      }
    }
  }

  @Path("/{version}/{id}?up")
  @ApiOperation(
    value="get entities upstream of a specified entity",
    nickname="findUpstream",
    httpMethod="GET",
    response=classOf[GenericRelEnt],
    responseContainer="List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name="version", required=true, dataType="string", paramType="path", value="API version", allowableValues=ApiVersions),
    new ApiImplicitParam(name="id", required=true, dataType="string", paramType="path", value="vault ID")))
  @ApiResponses(Array(
    new ApiResponse(code=200, message="Successful"),
    new ApiResponse(code=404, message="Not Found"),
    new ApiResponse(code=500, message="Internal Error")))
  def findUpstreamRoute = {
    pathVersion(ApiPrefix,DefaultVersion,Segment) { (version, guid) =>
      get {
        parameters('up) { up =>
          respondWithMediaType(`application/json`) {
            complete {
              DataManagementController.findUpstream(guid).toJson.prettyPrint
            }
          }
        }
      }
    }
  }

  @Path("/{version}/{id}?down")
  @ApiOperation(
    value="get entities downstream of a specified entity",
    nickname="findDownstream",
    httpMethod="GET",
    response=classOf[GenericRelEnt],
    responseContainer="List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name="version", required=true, dataType="string", paramType="path", value="API version", allowableValues=ApiVersions),
    new ApiImplicitParam(name="id", required=true, dataType="string", paramType="path", value="vault ID")))
  @ApiResponses(Array(
    new ApiResponse(code=200, message="Successful"),
    new ApiResponse(code=404, message="Not Found"),
    new ApiResponse(code=500, message="Internal Error")))
  def findDownstreamRoute = {
    pathVersion(ApiPrefix,DefaultVersion,Segment) { (version, guid) =>
      get {
        parameters('down) { down =>
          respondWithMediaType(`application/json`) {
            complete {
              DataManagementController.findDownstream(guid).toJson.prettyPrint
            }
          }
        }
      }
    }
  }

}
