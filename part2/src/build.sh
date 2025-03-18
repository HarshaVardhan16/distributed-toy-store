#!/bin/bash

# Define directories and their corresponding run commands
declare -A services=(
  ["toystore"]="java -cp ./target/toystore-1.0-SNAPSHOT-jar-with-dependencies.jar toystore.src.main.java.com.toystore.FrontendService"
  ["catalogservice"]="java -cp ./target/catalogservice-1.0-SNAPSHOT-jar-with-dependencies.jar com.catalogservice.CatalogService"
  ["orderservice"]="java -cp ./target/orderservice-1.0-SNAPSHOT-jar-with-dependencies.jar com.orderservice.OrderService"
  ["client"]="java -cp ./target/client-1.0-SNAPSHOT-jar-with-dependencies.jar com.client.Client"
)

# Loop through the services array
for SERVICE in "${!services[@]}"; do
  echo "Processing $SERVICE"
  cd "$SERVICE" || exit
  
  # Perform Maven clean, compile, and install
  ./mvnw clean compile install
  
  # Run the specific command for the microservice
  
  # Return to the parent directory
  cd ..
done



echo "All microservices are being processed!"
