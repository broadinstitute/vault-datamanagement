package org.broadinstitute.dsde.vault.datamanagement

import com.typesafe.config.ConfigFactory
import org.broadinstitute.dsde.vault.common.util.ConfigUtil

object DataManagementConfig {
  private val config = ConfigFactory.load()

  object HttpConfig {
    private val httpConfig = config.getConfig("http")
    lazy val interface = httpConfig.getString("interface")
    lazy val port = httpConfig.getInt("port")
    lazy val timeoutSeconds = httpConfig.getLong("timeoutSeconds")

  }

  //Config Settings

  object SwaggerConfig {
    private val swagger = config.getConfig("swagger")
    lazy val apiVersion = swagger.getString("apiVersion")
    lazy val swaggerVersion = swagger.getString("swaggerVersion")
    lazy val info = swagger.getString("info")
    lazy val description = swagger.getString("description")
    lazy val termsOfServiceUrl = swagger.getString("termsOfServiceUrl")
    lazy val contact = swagger.getString("contact")
    lazy val license = swagger.getString("license")
    lazy val licenseUrl = swagger.getString("licenseUrl")
    lazy val baseUrl = swagger.getString("baseUrl")
    lazy val apiDocs = swagger.getString("apiDocs")
  }

  object DatabaseConfig {
    private val database = config.getConfig("database")
    lazy val slickDriver = database.getString("slick.driver")
    lazy val liquibaseSetup = database.hasPath("liquibase")
    lazy val liquibaseChangeLog = database.getString("liquibase.changelog")
    lazy val liquibaseConnection = ConfigUtil.getStringOrElse(database, "liquibase.connection", "liquibase.database.jvm.JdbcConnection")
    lazy val jdbcUrl = database.getString("jdbc.url")
    lazy val jdbcDriver = database.getString("jdbc.driver")
    lazy val jdbcUser = ConfigUtil.getStringOrElse(database, "jdbc.user", null)
    lazy val jdbcPassword = ConfigUtil.getStringOrElse(database, "jdbc.password", null)
    lazy val c3p0MaxStatementsOption = ConfigUtil.getIntOption(database, "c3p0.maxStatements")
    lazy val entitiesPageLimitDefault = ConfigUtil.getIntOption(database, "entities.pageLimitDefault")
  }

  object ElasticSearchConfig {
    private val elasticsearch = config.getConfig("elasticsearch")
    lazy val clusterName = elasticsearch.getString("clusterName")
    lazy val server = elasticsearch.getString("server")
    lazy val port = elasticsearch.getString("port")
    lazy val indexName = elasticsearch.getAnyRef("indexName")
  }



}
