package org.broadinstitute.dsde.vault.datamanagement

import org.scalatest._
import spray.testkit.ScalatestRouteTest

abstract class DataManagementFreeSpec extends FreeSpec with Matchers with OptionValues with Inside with Inspectors with ScalatestRouteTest
