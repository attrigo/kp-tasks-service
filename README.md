# KP-TASKS-SERVICE

kp-tasks-service is a REST service application that publishes tasks operations. \
These operations are exposed via HTTP endpoints instrumented in a reactive way. \
There are also exposed a bunch of non-business endpoints which are produced by
Spring Boot Actuator.

The architecture is mainly based on Java 21 and Spring Boot 3.2, and it follows some of the [Microservice Architecture Principles](https://microservices.io/): \
The [Database per Service](https://microservices.io/patterns/data/database-per-service.html) pattern, therefore the Database is implemented by the service
itself, specifically the management of the database changes are delegated on Liquibase.

## Requirements

***

* [Java 21 (Java SDK 21)](https://www.oracle.com/es/java/technologies/downloads/#java21)
* [Apache Maven](https://maven.apache.org/download.cgi)
* [Docker](https://www.docker.com/)
* [Docker Compose](https://docs.docker.com/compose/)

## Installation

1. Clone the project:
    ```sh
    git clone https://github.com/attrigo/kp-tasks-service.git
    ```

2. Navigate to the project:
    ```sh
    cd kp-tasks-service
    ```

3. Install the project:
    ```sh
    mvn clean install
    ```

## Getting Started

***

### Start up

The application can be started in dev or docker mode

* Development mode.
    1. Start an standalone database:
        ```sh
        docker-compose up -d kp-tasks-postgres-db
        ```

    2. Launch the main class [KpTasksServiceApplication](src/main/java/com/bcn/kp/KpTasksServiceApplication.java).

* Docker mode.
  > This mode starts up the application and all its services at once

    1. Build the application's docker image:
        ```sh
        mvn spring-boot:build-image
        ```

    2. Launch application's docker-compose:
        ```sh
        docker-compose up -d
        ```

### Usage

The project brings with an embedded [Swagger UI](https://swagger.io/tools/swagger-ui/), a web tool that facilitates the visualization and interaction of the
HTTP endpoints.

You can use this [Swagger UI](http://localhost:8080/kp-tasks-service/swagger-ui.html) or any other HTTP client to consume the API.

### Shut down and clean

In order to avoid wasting local machine resources it is recommended to stop all started up Docker services once they are not necessary anymore

To stop the standalone database service:

```sh
docker-compose stop kp-tasks-postgres-db
```

To delete he standalone database service and its volume:

```sh
docker-compose down -v kp-tasks-postgres-db
```

To stop all Docker service:

```sh
docker-compose stop
```

To remove all Docker service and it's volumes:

```sh
docker-compose down -v
```

> The optional -v flag deletes the service volumes, so during next start up they will have to be created from scratch.

## Development features

***

### Generate Docker image

To build the Docker image:

```sh
mvn spring-boot:build-image
```

### Start up a standalone database

To start up the database in standalone mode:

```sh
docker-compose up -d kp-tasks-postgres-db
```

> This option creates an empty database, to update the database with the appropriate objects use Liquibase.

### Managing Database changes (Liquibase)

To apply the changes:

```sh
mvn liquibase:update
```

To roll back the changes:

```sh
mvn liquibase:rollback
```

> Liquibase executions must be done through the maven plugin.

### Generate the test coverage report

To launch the tests and generate the coverage report:

1. Generate the test report:
    ```sh
    mvn clean verify
    ```

2. Open the report: [index.html](target/site/jacoco-aggregate/index.html)

> The coverage report includes unit tests and integration tests

### Generate the Javadoc

To generate the Javadoc:

1. Generate the Javadoc files:
    ```sh
    mvn clean package
    ```

2. Open the Javadoc: [index.html](target/site/apidocs/index.html)

### Format code

The project uses [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven) to properly format Java code following style defined
in [kp_formatter.xml](kp_formatter.xml) file.

* To check code style: identifies code not well formatted.
    ```sh
    mvn spotless:check
    ```

* To format files: formats any non-formatted code.
    ```sh
    mvn spotless:apply
    ```

## Versioning

***

The project follows [Semantic Versioning](https://semver.org/) and [Trunk Based Development](https://trunkbaseddevelopment.com/) branching strategy.

The releases are manually managed using the [Branch For Release](https://trunkbaseddevelopment.com/branch-for-release/) approach.

## Contributing

To ensure the project standards, the code quality and the correct application behaviour there are several validations that any modification must pass before
adding it to the main branch.

There are a few local validations triggered as [git hooks](git/hooks) that checks:

1. Code follows specific code style.
2. Commit message pursues [Conventional Commits standard](https://www.conventionalcommits.org/en/v1.0.0/).

There is also a CI [pipeline](.github/workflows/ci.yml) triggered as GitHub actions that checks:

1. Code compiles and tests pass.
2. Liquibase changesets could be applied and rollback.

## Resources

***

### Reference Documentation

For further reference, please consider the following sections:

* Spring Boot
    * [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
    * [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#actuator)
    * [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools)
    * [Spring Boot Test](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
* Spring
    * [Spring Reactive Web](https://docs.spring.io/spring-framework/reference/web/webflux.html)
    * [Spring Data R2DBC](https://docs.spring.io/spring-data/relational/reference/r2dbc.html)
    * [Spring Validation](https://docs.spring.io/spring-framework/reference/core/validation.html)
    * [Spring OpenAPI](https://springdoc.org/)
* Database
    * [PostgresQL](https://www.postgresql.org/docs/current/)
    * [Liquibase Migration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/index.html#howto.data-initialization.migration-tool.liquibase)
* Testing
    * [Junit](https://junit.org/junit5/docs/current/user-guide/)
    * [Mockito](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
    * [TestContainers](https://java.testcontainers.org/)
* Tools
    * [MapStruct](https://mapstruct.org/documentation/)
    * [Lombok](https://projectlombok.org/features/)

## License

***

kp-tasks-service is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0").
