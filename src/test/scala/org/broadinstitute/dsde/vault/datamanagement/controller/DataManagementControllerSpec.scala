package org.broadinstitute.dsde.vault.datamanagement.controller

import org.broadinstitute.dsde.vault.datamanagement.domain.{TestDatabase, Attribute, Entity}
import org.broadinstitute.dsde.vault.datamanagement.DataManagementFreeSpec
import java.sql.Timestamp
import java.util.UUID

class DataManagementControllerSpec extends DataManagementFreeSpec with TestDatabase {

  def actorRefFactory = system

  "DataManagementController" - {
    "should insert and retrieve an attribute" in {
      val da = DataManagementController.dataAccess
      val db = DataManagementController.database
      db withTransaction {
        implicit session => {
          val entityOrig = Entity("aType", "me")
          val entityInsert = da.insert(entityOrig)
          val attrOrig = Attribute(entityOrig.guid, "testName", "testValue")
          val attrInsert = da.insert(attrOrig)
          val attrSelect = da.getAttribute(attrInsert.id.get)

          attrOrig.id should be (empty)
          attrInsert.id shouldNot be (empty)
          attrSelect.id should be (attrInsert.id)
        }
      }
    }
  }
}
