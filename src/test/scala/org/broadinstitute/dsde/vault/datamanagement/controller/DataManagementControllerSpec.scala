package org.broadinstitute.dsde.vault.datamanagement.controller

import org.broadinstitute.dsde.vault.datamanagement.domain._
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
          // TODO: Validate that the orig and insert are java .equals and not java ==
          val entityOrig = Entity("aType", "me")
          val entityInsert = da.insert(entityOrig)
          val parentOrig = Entity("bType", "parent")
          val parentInsert = da.insert(parentOrig)
          val parentOfEntityOrig = Entity("cType", "parentOfEntity")
          val parentOfEntityInsert = da.insert(parentOfEntityOrig)
          val relationOrig = Relation(
            parentOfEntityOrig.guid,
            parentInsert.guid,
            entityInsert.guid)
          val relationInsert = da.insert(relationOrig)
          val attrOrig = Attribute(entityInsert.guid, "testName", "testValue")
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
