package org.broadinstitute.dsde.vault.datamanagement.domain

import java.sql.{Connection, DriverManager}

import liquibase.Liquibase
import liquibase.database.jvm.HsqlConnection
import liquibase.resource.{FileSystemResourceAccessor, ResourceAccessor}
import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.DatabaseConfig

trait HsqlTestDatabase {
  HsqlTestDatabase.checkStarted()
}

// Modified from https://tillias.wordpress.com/2012/11/10/unit-testing-and-integration-testing-using-junit-liquibase-hsqldb-hibernate-maven-and-spring-framework/
object HsqlTestDatabase {

  private var holdingConnection: Connection = _
  private var liquibase: Liquibase = _

  def checkStarted() {
    // do nothing, static constructor run of start() does actual work
  }

  start()

  private def start(): Unit = {
    setUp("test")
  }

  private def setUp(contexts: String) {
    val resourceAccessor: ResourceAccessor = new FileSystemResourceAccessor()
    Class.forName(DatabaseConfig.jdbcDriver)

    holdingConnection = getConnectionImpl
    val hsconn: HsqlConnection = new HsqlConnection(holdingConnection)
    liquibase = new Liquibase(DatabaseConfig.liquibaseChangeLog, resourceAccessor, hsconn)
    liquibase.dropAll()
    liquibase.update(contexts)
    hsconn.close()
  }

  private def getConnectionImpl: Connection = {
    DriverManager.getConnection(
      DatabaseConfig.jdbcUrl,
      DatabaseConfig.jdbcUser,
      DatabaseConfig.jdbcPassword)
  }
}
