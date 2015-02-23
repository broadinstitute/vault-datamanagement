# vault-datamanagement
Vault Data Management (DM) Service

## IntelliJ Setup
* Configure the SBT plugin
* Open this source directory with File -> Open
* In the Import Project dialog, check "Create directories for empty content roots automatically" and set your Project SDK to 1.8 (TODO: do we actually require 1.8?)

## Bash Setup
Ensure the following prerequisites are installed. On a Mac this may easily be done via [Homebrew](http://brew.sh).
* [sbt](http://scala-sbt.org)
* [liquibase](http://liquibase.org)

## Plugins
* [spray-routing](http://spray.io/documentation/1.2.2/spray-routing/)
* [spray-json](https://github.com/spray/spray-json)
* [sbt-assembly](https://github.com/sbt/sbt-assembly)
* [sbt-revolver](https://github.com/spray/sbt-revolver)
* [spray-swagger](https://github.com/gettyimages/spray-swagger)
* [slick](https://typesafe.com/community/core-tools/slick)
* [liquibase](http://liquibase.org)
* [scalatest](http://scalatest.org)

## Development Notes
* Configuration is excluded from the build package:
    - When running via sbt, start sbt with the config file ```sbt -Dconfig.file=src/main/resources/application.conf``` and the run command will pick up your local configuration.
    - When running via sbt/revolver (i.e. using the re-start command), you can just run in sbt normally - the config is preset for you in build.sbt. You still must create the test database first.

## Building and Running

### Creating test database

Make sure the hsqldb driver has been resolved by sbt.
```bash
sbt update
```

Run liquibase to create a temporary database:
```bash
liquibase \
    --classpath=${HOME}/.m2/repository/org/hsqldb/hsqldb/2.3.2/hsqldb-2.3.2.jar \
    --changeLogFile=src/main/migrations/changelog.xml \
    --driver=org.hsqldb.jdbcDriver \
    --url="jdbc:hsqldb:file:target/hsqldb/temp.db" \
    update
```

### Debugging

For debugging, use sbt-revolver within the sbt console:
```bash
sbt
> re-start
```

### Running

Run the assembly task to build a fat jar:
```bash
sbt assembly
```

Execute the jar with the path to the jar and path for the config file:
```bash
java \
    -Dconfig.file=src/main/resources/application.conf \
    -jar target/scala-2.11/Vault-DataManagement-assembly-0.1.jar
```

### Accessing REST API

After starting the server using sbt-revolver or executing the jar, open the swagger interface in your browser:

[http://localhost:8081/swagger](http://localhost:8081/swagger)
