name          := "Vault-DataManagement"

version       := "0.1"

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV
    ,"io.spray"            %%  "spray-routing" % sprayV
    ,"io.spray"            %%  "spray-json"    % "1.3.1"
    ,"io.spray"            %%  "spray-client"  % sprayV
    ,"io.spray"            %%  "spray-testkit" % sprayV  % "test"
    ,"com.typesafe.akka"   %%  "akka-actor"    % akkaV
    ,"org.scalatest"       %%  "scalatest"     % "2.2.1" % "test"
    ,"com.gettyimages"     %%  "spray-swagger" % "0.5.0"
    ,"org.webjars"         %   "swagger-ui"    % "2.1.8-M1"
    // -- Logging --
    ,"ch.qos.logback" % "logback-classic" % "1.1.2" % "provided"
    ,"com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
    ,"com.typesafe.slick" %% "slick" % "2.1.0"
    ,"c3p0" % "c3p0" % "0.9.1.2"
    ,"org.hsqldb" % "hsqldb" % "2.3.2" // % "test" TODO: Using for dev assembly, at the moment
    ,"org.postgresql" % "postgresql" % "9.4-1200-jdbc41" exclude("org.slf4j", "slf4j-simple")
    ,"org.liquibase" % "liquibase-core" % "3.3.2" % "test"
  )
}

Revolver.settings

// Don't package the application.conf in the assembly
excludeFilter in (Compile, unmanagedResources) := HiddenFileFilter || "application.conf"

// Do include the application.conf for testing purposes
excludeFilter in (Test, unmanagedResources) := HiddenFileFilter

// Make the application.conf available to revolver
javaOptions in Revolver.reStart += "-Dconfig.file=src/main/resources/application.conf"
