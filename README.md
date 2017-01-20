# Axon Bank
This is a sample application with the purpose of showing the capabilities of Axon Framework.

## Domain
As you may have guessed from the name of the application, Axon Bank concerns itself with the banking domain. The application consists of 2 aggregates: bank account and bank transfer. We have tried to find an appropriate balance between complexity and simplicity. The application is meant to be complex enough to showcase interesting building blocks provided by Axon Framework. But we did not want people to get lost in the business logic of the application.

## Technical Details
We have tried to keep the application self-contained. It is built with Spring Boot and therefore does not require you to have Tomcat installed.

There are two ways to run the application: on a single node and on multiple nodes using Docker.

Running the application on a single node does not require you to install any dependencies. The storage of data is all done in memory and does not require you to run an external data store.

The distributed version requires Docker and Docker Compose to be installed. There are 4 containers involved in the distributed version: 1 container running MySQL, 1 container running RabbitMQ and 2 containers running instances of Axon Bank. The MySQL server is used for the storage of events, sagas and query side data. RabbitMQ acts as a dedicated STOMP broker. STOMP is used in combination with WebSockets. The dedicated STOMP broker is needed to keep the interfaces of both Axon Bank instances in sync. The commands are distributed between the containers running Axon Bank with the DistributedCommandBus.

## Usage
In order to run the single node version you may execute the following commands: 
* `mvn clean install`
* `mvn -pl web spring-boot:run`.

The distributed version can be run using the following commands:

* `mvn clean install`
* `mvn -pl web docker:build`
* `docker-compose up db` (this will create and initialize the db container, you can stop the container after it has been initialized)
* `docker-compose up`

Once all containers are running you can access each instance of Axon Bank by visiting [http://localhost:8080/](http://localhost:8080/) and [http://localhost:8081/](http://localhost:8081/).
