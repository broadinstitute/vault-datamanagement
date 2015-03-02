package org.broadinstitute.dsde.vault.datamanagement

import com.typesafe.config.{Config, ConfigFactory}

object DataManagementConfig {
  private val config = ConfigFactory.load()
  private def getOrElse(config: Config, path: String, default: String): String = {
    if (config.hasPath(path)) config.getString(path) else default
  }

  object HttpConfig {
    private val httpConfig = config.getConfig("http")
    lazy val interface = httpConfig.getString("interface")
    lazy val port = httpConfig.getInt("port")
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
    lazy val liquibaseConnection = getOrElse(database, "liquibase.connection", "liquibase.database.jvm.JdbcConnection")
    lazy val jdbcUrl = database.getString("jdbc.url")
    lazy val jdbcDriver = database.getString("jdbc.driver")
    lazy val jdbcUser = getOrElse(database, "jdbc.user", null)
    lazy val jdbcPassword = getOrElse(database, "jdbc.password", null)
  }

}
