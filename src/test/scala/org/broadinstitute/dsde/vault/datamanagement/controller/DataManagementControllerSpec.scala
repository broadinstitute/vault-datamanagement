package org.broadinstitute.dsde.vault.datamanagement.controller

import org.broadinstitute.dsde.vault.datamanagement.domain.{HsqlTestDatabase, Attribute}
import org.broadinstitute.dsde.vault.datamanagement.DataManagementFreeSpec

class DataManagementControllerSpec extends DataManagementFreeSpec with HsqlTestDatabase {

  def actorRefFactory = system

  "DataManagementController" - {
    "should insert and retrieve an attribute" in {
      val da = DataManagementController.dataAccess
      val db = DataManagementController.database
      db withTransaction {
        implicit session => {
          val attrOrig = new Attribute("testName", "testValue")
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
