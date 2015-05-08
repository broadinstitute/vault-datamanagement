package org.broadinstitute.dsde.vault.datamanagement

import org.broadinstitute.dsde.vault.common.openam.{OpenAMConfig, OpenAMSession}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import spray.testkit.ScalatestRouteTest

import scala.concurrent.duration._

abstract class DataManagementFreeSpec extends FreeSpec with Matchers with OptionValues with Inside with Inspectors with ScalaFutures with ScalatestRouteTest with PropertyChecks {
  def actorRefFactory = system
  protected lazy val openAMSession = OpenAMSession(()).futureValue(timeout(scaled(OpenAMConfig.timeoutSeconds.seconds)), interval(scaled(0.5.seconds)))
  protected def v(version: Option[Int]): String = version.map("/v" + _).getOrElse("")
}
