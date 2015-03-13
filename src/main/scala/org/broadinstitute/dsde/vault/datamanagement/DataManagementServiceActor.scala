package org.broadinstitute.dsde.vault.datamanagement

import akka.actor.ActorLogging
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo
import org.broadinstitute.dsde.vault.datamanagement.services._
import spray.routing.HttpServiceActor

import scala.reflect.runtime.universe._


//the actor which will accept request and distribute to other actors/objects
class DataManagementServiceActor extends HttpServiceActor with ActorLogging {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  override def actorRefFactory = context

  val helloWorld = new HelloWorldService {
    def actorRefFactory = context
  }

  val unmappedBAM = new UnmappedBAMService {
    def actorRefFactory = context
  }

  // this actor runs all routes
  def receive = runRoute(
    unmappedBAM.routes ~
      helloWorld.routes ~
      swaggerService.routes ~
      get {
        pathPrefix("swagger") {
          pathEndOrSingleSlash {
            getFromResource("swagger/index.html")
          }
        } ~ getFromResourceDirectory("swagger")
      })

  val swaggerService = new SwaggerHttpService {
    // All documented API services must be added to these API types
    override def apiTypes = Seq(typeOf[UnmappedBAMService], typeOf[HelloWorldService])

    override def apiVersion = DataManagementConfig.SwaggerConfig.apiVersion

    override def baseUrl = DataManagementConfig.SwaggerConfig.baseUrl

    override def docsPath = DataManagementConfig.SwaggerConfig.apiDocs

    override def actorRefFactory = context

    override def apiInfo = Some(
      new ApiInfo(
        DataManagementConfig.SwaggerConfig.info,
        DataManagementConfig.SwaggerConfig.description,
        DataManagementConfig.SwaggerConfig.termsOfServiceUrl,
        DataManagementConfig.SwaggerConfig.contact,
        DataManagementConfig.SwaggerConfig.license,
        DataManagementConfig.SwaggerConfig.licenseUrl)
    )
  }

}
