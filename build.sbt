name := "Vault-DataManagement"

val vaultOrg = "org.broadinstitute.dsde.vault"

organization := vaultOrg

// Canonical version
val versionRoot = "0.1"

// Get the revision, or -1 (later will be bumped to zero)
val versionRevision = ("git rev-list --count HEAD" #|| "echo -1").!!.trim.toInt

// Set the suffix to None...
val versionSuffix = {
  try {
    // ...except when there are no modifications...
    if ("git diff --quiet HEAD".! == 0) {
      // ...then set the suffix to the revision "dash" git hash
      Option(versionRevision + "-" + "git rev-parse --short HEAD".!!.trim)
    } else {
      None
    }
  } catch {
    case e: Exception =>
      None
  }
}

// Set the composite version
version := versionRoot + "-" + versionSuffix.getOrElse((versionRevision + 1) + "-SNAPSHOT")

val artifactory = "https://artifactory.broadinstitute.org/artifactory/"

resolvers += "artifactory-releases" at artifactory + "libs-release"

scalaVersion := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    vaultOrg %% "vault-common" % "0.1-17-0ee4ad5"
    , "io.spray" %% "spray-can" % sprayV
    , "io.spray" %% "spray-routing" % sprayV
    , "io.spray" %% "spray-json" % "1.3.1"
    , "io.spray" %% "spray-client" % sprayV
    , "io.spray" %% "spray-testkit" % sprayV % "test"
    , "com.typesafe.akka" %% "akka-actor" % akkaV
    , "com.typesafe.akka" %% "akka-slf4j" % akkaV
    , "org.scalatest" %% "scalatest" % "2.2.1" % "test"
    , "com.gettyimages" %% "spray-swagger" % "0.5.0"
    , "org.webjars" % "swagger-ui" % "2.1.8-M1"
    // -- Logging --
    , "ch.qos.logback" % "logback-classic" % "1.1.2" % "provided"
    , "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
    , "com.typesafe.slick" %% "slick" % "2.1.0"
    , "c3p0" % "c3p0" % "0.9.1.2"
    , "org.hsqldb" % "hsqldb" % "2.3.2" // % "test" TODO: Using for dev assembly, at the moment
    , "org.postgresql" % "postgresql" % "9.4-1200-jdbc41" exclude("org.slf4j", "slf4j-simple")
    , "org.liquibase" % "liquibase-core" % "3.3.2" % "test"
    , "org.elasticsearch" % "elasticsearch" % "1.4.4"
  )
}

Revolver.settings.settings

// Don't package the application.conf in the assembly
excludeFilter in(Compile, unmanagedResources) := HiddenFileFilter || "application.conf"

// Do include the application.conf for testing purposes
excludeFilter in(Test, unmanagedResources) := HiddenFileFilter

// Make the application.conf available to revolver
javaOptions in Revolver.reStart += "-Dconfig.file=src/main/resources/application.conf"

// Copy over various properties
val copyProperties = Seq("openam.deploymentUri")

javaOptions in Revolver.reStart ++= new scala.sys.SystemProperties()
  .filterKeys(key => copyProperties.contains(key) || copyProperties.exists(prefix => key.startsWith(prefix + ".")))
  .map { case (key, value) => s"-D$key=$value" }
  .toSeq
