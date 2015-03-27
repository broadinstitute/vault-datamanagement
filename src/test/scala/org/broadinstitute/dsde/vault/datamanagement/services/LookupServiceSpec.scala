package org.broadinstitute.dsde.vault.datamanagement.services

import java.util.UUID

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec
import org.broadinstitute.dsde.vault.datamanagement.controller.DataManagementController
import org.broadinstitute.dsde.vault.datamanagement.model._
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._

class LookupServiceSpec extends DataManagementDatabaseFreeSpec with LookupService {

  def actorRefFactory = system

  "LookupBAMService" - {
    "when accessing the /query/{entityType}/{attributeName}/{attributeValue} path" - {
      val testValue = UUID.randomUUID().toString
      val metadata = Map("sameKey" -> "sameValue", "uniqueTest" -> testValue)
      val entityGUID = DataManagementController.createUnmappedBAM(UnmappedBAM(Map.empty, metadata), "LookupServiceSpec").id.get

      "Lookup should return previously stored unmapped BAM" in {
        Get(s"/query/ubam/uniqueTest/$testValue") ~> lookupEntityByTypeAttributeNameValueRoute ~> check {
          val entitySearchResult = responseAs[EntitySearchResult]
          entitySearchResult.guid should be(entityGUID)
          entitySearchResult.`type` should be("ubam")
        }
      }

      "Lookup of unknown entity type should return not found" in {
        Get(s"/query/ubam_similar/uniqueTest/$testValue") ~> sealRoute(lookupEntityByTypeAttributeNameValueRoute) ~> check {
          status should be(NotFound)
        }
      }

      "Lookup of unknown attribute name return not found" in {
        Get(s"/query/ubam/uniqueTest_similar/$testValue") ~> sealRoute(lookupEntityByTypeAttributeNameValueRoute) ~> check {
          status should be(NotFound)
        }
      }

      "Lookup of unknown attribute value should return not found" in {
        Get("/query/ubam/uniqueTest/unknownValue") ~> sealRoute(lookupEntityByTypeAttributeNameValueRoute) ~> check {
          status should be(NotFound)
        }
      }

      "Lookup of mismatched attribute name + value should return not found" in {
        Get(s"/query/ubam/ownerId/$testValue") ~> sealRoute(lookupEntityByTypeAttributeNameValueRoute) ~> check {
          status should be(NotFound)
        }
      }
    }
  }
}
