package org.broadinstitute.dsde.vault.datamanagement

import java.sql.{Connection, DriverManager}

import liquibase.Liquibase
import liquibase.database.DatabaseConnection
import liquibase.resource.{FileSystemResourceAccessor, ResourceAccessor}
import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.DatabaseConfig

trait TestDatabase {
  TestDatabase.checkStarted()
}

// Modified from https://tillias.wordpress.com/2012/11/10/unit-testing-and-integration-testing-using-junit-liquibase-hsqldb-hibernate-maven-and-spring-framework/
object TestDatabase {

  private var holdingConnection: Connection = _
  private var liquibase: Liquibase = _

  def checkStarted() {
    // do nothing, static constructor run of start() does actual work
  }

  start()

  private def start(): Unit = {
    if (DatabaseConfig.liquibaseSetup)
      setUp("test")
  }

  private def setUp(contexts: String) {
    val resourceAccessor: ResourceAccessor = new FileSystemResourceAccessor()
    Class.forName(DatabaseConfig.jdbcDriver)

    holdingConnection = getConnectionImpl
    val connectionClass = Class.forName(DatabaseConfig.liquibaseConnection)
    val connectionConstructor = connectionClass.getConstructor(classOf[Connection])
    val conn: DatabaseConnection = connectionConstructor.newInstance(holdingConnection).asInstanceOf[DatabaseConnection]
    liquibase = new Liquibase(DatabaseConfig.liquibaseChangeLog, resourceAccessor, conn)
    liquibase.dropAll()
    liquibase.update(contexts)
    conn.close()
  }

  private def getConnectionImpl: Connection = {
    DriverManager.getConnection(
      DatabaseConfig.jdbcUrl,
      DatabaseConfig.jdbcUser,
      DatabaseConfig.jdbcPassword)
  }
}
