package org.broadinstitute.dsde.vault.datamanagement.controller

import org.broadinstitute.dsde.vault.datamanagement.DataManagementDatabaseFreeSpec

class DataManagementControllerSpec extends DataManagementDatabaseFreeSpec {
  "DataManagementController" - {
    "should insert and retrieve an attribute" in {
      val da = DataManagementController.dataAccess
      val db = DataManagementController.database
      db withTransaction {
        implicit session => {
          val entityInsert = da.insertEntity("aType", "me")
          val parentInsert = da.insertEntity("bType", "parent")
          val parentOfEntityInsert = da.insertEntity("cType", "parentOfEntity")
          val relationInsert = da.insertRelation(
            parentOfEntityInsert.guid.get,
            parentInsert.guid.get,
            entityInsert.guid.get)
          val attrInsert = da.insertAttribute(entityInsert.guid.get, "testName", "testValue")
          val attrSelect = da.getAttribute(attrInsert.id.get)

          attrInsert.id shouldNot be(empty)
          attrSelect.id should be(attrInsert.id)
        }
      }
    }
  }
}
