package org.broadinstitute.dsde.vault.datamanagement.domain

import scala.slick.driver.JdbcProfile

trait DriverComponent {
  val driver: JdbcProfile
}
