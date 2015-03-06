package org.broadinstitute.dsde.vault.datamanagement.domain

import org.broadinstitute.dsde.vault.datamanagement.util.Reflection

import scala.slick.driver.JdbcProfile

class DataAccess(val driver: JdbcProfile)
  extends AttributeComponent
  with EntityComponent
  with RelationComponent
  with DriverComponent {

  def this(driverName: String) {
    this(Reflection.getObject[JdbcProfile](driverName))
  }
}
