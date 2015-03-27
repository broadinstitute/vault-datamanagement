# vault-datamanagement
Vault Data Management (DM) Service

## IntelliJ Setup
* Configure the SBT plugin
* Open this source directory with File -> Open
* In the Import Project dialog, check "Create directories for empty content roots automatically" and set your Project SDK to 1.8 (TODO: do we actually require 1.8?)

## Bash Setup
Ensure the following prerequisites are installed. On a Mac, `sbt` and `liquibase` may be easily installed via [Homebrew](http://brew.sh).
* [sbt](http://scala-sbt.org)
* [liquibase](http://liquibase.org)
* [openam](https://forgerock.org/openam)

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

### Running via sbt

* Configuration is excluded from the build package:
    - When running via `sbt`, start with the config file `sbt -Dconfig.file=src/main/resources/application.conf` and the run command will pick up your local configuration.
    - When running via sbt-revolver (i.e. using the re-start command), you can just run in `sbt` normally - the config is preset for you in build.sbt. You still must create a database first.

### Configuration of OpenAM

The repository `application.conf` does _not_ contain the deployment URI for OpenAM. Before running, configure the URI inside your local copy of `application.conf`.

However if you will be actively developing across the codebase, and don't want to edit the `application.conf`, you may also set the deployment URI via the command line or via the `.sbtopts` file.

Use the `-Dkey=value` syntax for setting system properties on the command line.

```bash
sbt \
    -Dopenam.deploymentUri=...
```

Alternatively, you may set system properties in `.sbtopts`. The additional options in the file will load every time you run `sbt`.

```bash
$ cat .sbtopts | grep openam.deploymentUri
-Dopenam.deploymentUri=...
$
```

The non-production test code is able to login to OpenAM and generate a token. Parts of the configuration for authenticating to the server are setup in the test `application.conf` file, except for the username and password. You must specify these two properties via `openam.username` and `openam.password`.

```bash
$ cat .sbtopts | grep openam
-Dopenam.deploymentUri=...
-Dopenam.username=...
-Dopenam.password=...
$
```

### Testing with in memory hsqldb

Run a basic test of all routes against hsqldb using the normal `test`. The tests will create and initialize an instance of the database in memory.

```bash
sbt \
    test
```

### Testing with postgresql

An example postgres configuration file is in the test resources directory. Update an instance of the postgres configuration file with the database info for your postgres installation. **WARNING:** By default the configuration in the example `postgres.conf` activates liquibase and reinitializes the database. Remove the `liquibase` settings to disable the tests from dropping the tables and recreating the database.

```bash
sbt \
    -Dconfig.file=src/test/resources/postgres.conf \
    test
```

## Building and Running

### Creating or updating the temporary database

Only the tests create a database from scratch. When running the production code, one must manually create or update the database using liquibase.

Make sure the hsqldb driver has been resolved by `sbt`.
```bash
sbt update
```

Run `liquibase` to create a temporary database:
```bash
liquibase \
    --classpath=${HOME}/.ivy2/cache/org.hsqldb/hsqldb/jars/hsqldb-2.3.2.jar \
    --changeLogFile=src/main/migrations/changelog.xml \
    --driver=org.hsqldb.jdbcDriver \
    --url="jdbc:hsqldb:file:target/hsqldb/temp.db" \
    update
```

### Debugging

For debugging, after manually creating or updating the temporary database using the command above, use sbt-revolver within the `sbt` shell:
```bash
sbt
> re-start
```

### Running

Run the assembly task to build a fat jar:
```bash
sbt assembly
```

After manually creating or updating the temporary database using the command above, execute the jar with the path to the jar and path for the config file:
```bash
java \
    -Dconfig.file=src/main/resources/application.conf \
    -jar target/scala-2.11/Vault-DataManagement-assembly-0.1.jar
```

### Accessing REST API

After starting the server using sbt-revolver or executing the jar, open the swagger interface in your browser:

[http://localhost:8081/swagger](http://localhost:8081/swagger)
