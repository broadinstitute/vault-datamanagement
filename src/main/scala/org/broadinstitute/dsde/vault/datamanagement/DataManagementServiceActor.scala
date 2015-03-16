package org.broadinstitute.dsde.vault.datamanagement

import akka.actor.ActorLogging
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo
import org.broadinstitute.dsde.vault.datamanagement.services._
import spray.http.StatusCodes._
import spray.routing.HttpServiceActor

import scala.reflect.runtime.universe._


//the actor which will accept request and distribute to other actors/objects
class DataManagementServiceActor extends HttpServiceActor with ActorLogging {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  override def actorRefFactory = context

  val unmappedBAM = new UnmappedBAMService {
    def actorRefFactory = context
  }

  val analysis = new AnalysisService {
    def actorRefFactory = context
  }

  val lookupService = new LookupService {
    def actorRefFactory = context
  }

  // this actor runs all routes
  def receive = runRoute(
    unmappedBAM.routes ~
      analysis.routes ~
      lookupService.routes ~
      swaggerService.routes ~
      swaggerUiService
  )

  val swaggerService = new SwaggerHttpService {
    // All documented API services must be added to these API types
    override def apiTypes = Seq(typeOf[UnmappedBAMService], typeOf[AnalysisService], typeOf[LookupService])

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

  val swaggerUiService = {
    get {
      pathPrefix("swagger") {
        // if the user just hits "swagger", redirect to the index page with our api docs specified on the url
        pathEndOrSingleSlash { p =>
          // dynamically calculate the context path, which may be different in various environments
          val path = p.request.uri.path.toString
          val dynamicContext = path.substring(0, path.indexOf("swagger"))
          p.redirect("/swagger/index.html?url=" + dynamicContext + "api-docs", TemporaryRedirect)
        } ~
          pathPrefix("swagger/index.html") {
            getFromResource("META-INF/resources/webjars/swagger-ui/2.1.8-M1/index.html")
          } ~
          getFromResourceDirectory("META-INF/resources/webjars/swagger-ui/2.1.8-M1")
      }
    }
  }

}
