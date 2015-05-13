package org.broadinstitute.dsde.vault.datamanagement
import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import scala.util.{Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object DataManagementApp extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("vault-datamanagement")

  // create and start our service actor
  val service = system.actorOf(Props[DataManagementServiceActor], "vault-datamanagement-service")

  implicit val timeout = Timeout(5.seconds)

  (IO(Http) ? Http.Bind(service,DataManagementConfig.HttpConfig.interface, DataManagementConfig.HttpConfig.port)).onComplete {
      case Success(Http.CommandFailed(_)) =>
        system.log.error("could not bind to port: "+DataManagementConfig.HttpConfig.port)
        system.shutdown()
    }

}
