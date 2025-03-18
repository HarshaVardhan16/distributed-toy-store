From the main folder of the Project

To build the services

CatalogService:

cd catalogservice
mvn clean package
java -jar ./target/catalogservice-0.0.1-SNAPSHOT.jar &

FrontEndService:

cd frontendservice
mvn clean package
java -jar ./target/catalogservice-0.0.1-SNAPSHOT.jar --cache.active=$cacheActive &

The cache.active variable is for cache activation

OrderService:

cd orderservice
mvn clean package
cd ..

Comeback to main folder

./start_order_service.sh &

this starts all the orderServices

./stop_service.sh $PORT_NUMBER 

stops a service at a portNumber

./start_service.sh <replicaId> <portNumber>

Start a new orderService replica with replicaId and portNumber

The orderservices are at portNumbers: 12346, 12347, 12348
The catalogservice is at port 12345
The frontendservice is at port 12343

For the client:

cd client
mvn clean package
./runMultiple.sh <no of clients> <probability of purchase> <No of queries>

This will run the client with desired probability and no of queries



AWS Deployment:

Started AWS lab

Copied the credentials and downloaded the labsuser.pem file

aws configure

Configured the aws instance

aws ec2 run-instances --image-id ami-0d73480446600f555 --instance-type t2.micro --key-name vockey > instance.json

Created t2.micro instance

In the AWS console created new Inbound rules to allow custom tcp traffic from ports 12340-12350

SSHed into my console to pull the git repo

ssh -i labsuser.pem ubuntu@ec2-54-159-33-11.compute-1.amazonaws.com

Git pulled my repo into the instance and started testing
