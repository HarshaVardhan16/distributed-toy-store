To run all the microservices:

First build all the microservices.

cd src

./build.sh

This builds all the jar files required for the microservices

To run the FrontEndService:

java -cp ./target/toystore-1.0-SNAPSHOT-jar-with-dependencies.jar toystore.src.main.java.com.toystore.FrontendService

To run the OrderService:

java -cp ./target/orderservice-1.0-SNAPSHOT-jar-with-dependencies.jar com.orderservice.OrderService

To run the CatalogService:

java -cp ./target/catalogservice-1.0-SNAPSHOT-jar-with-dependencies.jar com.catalogservice.CatalogService

The commands are available in the build.sh, if you need them.


To run the client:

cd client 

./runMultiple.sh <number_of_clients> 

like ./runMultiple.sh 5, for running 5 clients at a time.

To run the tests

from src directory,

cd test/test

./mvnw clean compile install

java -cp ./target/test-1.0-SNAPSHOT-jar-with-dependencies.jar com.test.MicroservicesAPITests

For running all the microservices

Just use 

docker compose build
docker compose up 
docker compose down 

in the ELSRV3 server. But for the elsrv3 server to build the images of the services, we first need the jars of all services. So running build.sh first and then running docker compose is advised.
 
