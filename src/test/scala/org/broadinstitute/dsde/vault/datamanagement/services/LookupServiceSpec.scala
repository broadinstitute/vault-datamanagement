package org.broadinstitute.dsde.vault.datamanagement.services

import java.util.UUID

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model._
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

class LookupServiceSpec extends DataManagementDatabaseFreeSpec with LookupService {
  "LookupBAMService" - {

    val versions = Table(
      "version",
      None,
      Option(1),
      Option(2)
    )

    forAll(versions) { (version: Option[Int]) =>
      val pathBase = "/query" + v(version)

      s"when accessing the $pathBase/{entityType}/{attributeName}/{attributeValue} path" - {
        val testValue = UUID.randomUUID().toString
        val metadata = Map("sameKey" -> "sameValue", "uniqueTest" -> testValue)
        val entityGUID = DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, metadata), "LookupServiceSpec", includeProperties = true).id.get

        "Lookup should return previously stored unmapped BAM" in {
          Get(s"$pathBase/ubam/uniqueTest/$testValue") ~> lookupEntityByTypeAttributeNameValueRoute ~> check {
            val entitySearchResult = responseAs[EntitySearchResult]
            entitySearchResult.guid should be(entityGUID)
            entitySearchResult.`type` should be("ubam")
          }
        }

        "Lookup of unknown entity type should return not found" in {
          Get(s"$pathBase/ubam_similar/uniqueTest/$testValue") ~> sealRoute(lookupEntityByTypeAttributeNameValueRoute) ~> check {
            status should be(NotFound)
          }
        }

        "Lookup of unknown attribute name return not found" in {
          Get(s"$pathBase/ubam/uniqueTest_similar/$testValue") ~> sealRoute(lookupEntityByTypeAttributeNameValueRoute) ~> check {
            status should be(NotFound)
          }
        }

        "Lookup of unknown attribute value should return not found" in {
          Get(s"$pathBase/ubam/uniqueTest/unknownValue") ~> sealRoute(lookupEntityByTypeAttributeNameValueRoute) ~> check {
            status should be(NotFound)
          }
        }

        "Lookup of mismatched attribute name + value should return not found" in {
          Get(s"$pathBase/ubam/ownerId/$testValue") ~> sealRoute(lookupEntityByTypeAttributeNameValueRoute) ~> check {
            status should be(NotFound)
          }
        }
      }

    }
  }
}
