package org.broadinstitute.dsde.vault.datamanagement.services

import com.wordnik.swagger.annotations._
import org.broadinstitute.dsde.vault.datamanagement.domain.UnmappedBAM
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.MediaTypes._
import spray.json._
import spray.routing._

@Api(value = "/ubams", description = "uBAM Service", produces = "application/json")
trait UnmappedBAMService extends HttpService {

  val routes = describeRoute ~ ingestRoute

  @ApiOperation(value = "Describes a uBAM's metadata and associated files.",
    nickname = "ubam_describe",
    httpMethod = "GET",
    produces = "application/json",
    response = classOf[UnmappedBAM],
    notes = "Supports arbitrary metadata keys, but this is not represented well in Swagger (see the 'additionalMetadata' note below)"
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
    path("ubams" / Segment) { id =>
      get {
        respondWithMediaType(`application/json`) {
          complete {
            UnmappedBAM(
              Map(
                "bam" -> "sample BOSS id 1",
                "bai" -> "sample BOSS id 2",
                "..." -> "more files"
              ),
              Map(
                "ownerId" -> "dummy ownerId",
                "md5" -> "dummy md5",
                "project" -> "dummy project",
                "individualAlias" -> "dummy individualAlias",
                "sampleAlias" -> "dummy sampleAlias",
                "readGroupAlias" -> "dummy readGroupAlias",
                "librayName" -> "dummy libraryName",
                "sequencingCenter" -> "dummy sequencingCenter",
                "platform" -> "dummy platform",
                "platformUnit" -> "dummy platformUnit",
                "runDate" -> "dummy runDate",
                "additionalMetaData" -> "..."
              ),
              Some(id)
            ).toJson.prettyPrint
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates uBAM objects", nickname = "ubam_ingest", httpMethod = "POST",
    produces = "application/json", consumes = "application/json", response = classOf[UnmappedBAM],
    notes = "Accepts a json packet as POST. Creates a Vault object with the supplied metadata.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", required = true, dataType = "org.broadinstitute.dsde.vault.datamanagement.domain.UnmappedBAM", paramType = "body", value = "uBAM to create")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Successful"),
    new ApiResponse(code = 400, message = "Malformed Input"),
    new ApiResponse(code = 500, message = "Vault Internal Error")
  ))
  def ingestRoute = {
    path("ubams") {
      post {
        respondWithMediaType(`application/json`) {
          complete {
            UnmappedBAM(
              Map(
                "bam" -> "boss:boss-id-1",
                "bai" -> "boss:boss-id-2",
                "..." -> "moreFiles"
              ),
              Map("ownerId" -> "dummy owner id"),
              Some("dummy vault id")
            ).toJson.prettyPrint
          }
        }
      }
    }
  }

}
