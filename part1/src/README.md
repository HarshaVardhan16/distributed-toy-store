# Build and Execution Process for Part1:

From src directory,

cd part1/demo

./mvnw clean

./mvnw compile

./mvnw install

This should create a demo-1.0-SNAPSHOT.jar in the target directory.

## To run the server:

java -cp "./target/demo-1.0-SNAPSHOT.jar" com.client_server.Server

## To run the client:

./runMultiple.sh <no_of clients> (Ex. ./runMultiple.sh 5). Be sure to change the permissions of runMultiple to execute. chmod +x runMultiple.sh

or run the client with:

java -cp "./target/demo-1.0-SNAPSHOT.jar" com.client_server.Client

# Build and Execution Process for Part2:

From src directory,

cd part2/grpc

./mvnw clean

./mvnw compile

./mvnw install

This should create a grpc-1.1-SNAPSHOT-jar-with-dependencies.jar in the target directory.

## To run the Server:

java -cp grpc-1.1-SNAPSHOT-jar-with-dependencies.jar grpc.ToyStoreServer

## To run the client:

./runMultiple.sh <no_of clients> (Ex. ./runMultiple.sh 5). Be sure to change the permissions of runMultiple to execute. chmod +x runMultiple.sh

or run the client with:

java -cp "./target/grpc-1.1-SNAPSHOT-jar-with-dependencies.jar" grpc.ToyStoreClient