package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.domain.{HsqlTestDatabase, Attribute}
import org.broadinstitute.dsde.vault.datamanagement.services.JsonImplicits._
import org.broadinstitute.dsde.vault.datamanagement.DataManagementFreeSpec
import spray.httpx.SprayJsonSupport._
import spray.httpx.unmarshalling._

class HelloAttributeServiceSpec extends DataManagementFreeSpec with HelloAttributeService with HsqlTestDatabase {

  def actorRefFactory = system

  "HelloAttributeService" - {
    "when accessing the /helloattribute path" - {
      var createdId: Int = -1

      "PUT should store a new attribute" in {
        Put("/helloattribute", Attribute("foo", "bar")) ~> createRoute ~> check {
          val attribute = responseAs[Attribute]
          attribute.name should be("foo")
          attribute.value should be("bar")
          attribute.id shouldNot be(empty)
          createdId = attribute.id.get
        }
      }

      "GET should retrieve the previously stored attribute" in {
        assume(createdId >= 0)

        Get("/helloattribute/" + createdId) ~> readRoute ~> check {
          val attribute = responseAs[Attribute]
          attribute.name should be("foo")
          attribute.value should be("bar")
          attribute.id.value should be(createdId)
        }
      }
    }
  }
}
