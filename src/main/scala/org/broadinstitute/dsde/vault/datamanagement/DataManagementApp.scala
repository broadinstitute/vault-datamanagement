package org.broadinstitute.dsde.vault.datamanagement

import akka.actor.{ActorSystem, Props}
import scala.concurrent.duration.{FiniteDuration,SECONDS}
import org.broadinstitute.dsde.vault.common.util.ServerInitializer


object DataManagementApp extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("vault-datamanagement")
  val timeoutDuration = FiniteDuration(DataManagementConfig.HttpConfig.timeoutSeconds,SECONDS)

  // start a new HTTP server on configuration port with our service actor as the handler
  ServerInitializer.startWebServiceActors(Props[DataManagementServiceActor], DataManagementConfig.HttpConfig.interface, DataManagementConfig.HttpConfig.port, timeoutDuration, system)

}