#!/bin/bash

# Function to start the orderservice
start_orderservice() {
  local replica_id=$1
  local port_number=$2

  java -jar ./orderservice/target/orderservice-0.0.1-SNAPSHOT.jar --server.port=$port_number --replica.id=$replica_id &
  echo "$port_number:$!" >> pid_port_mapping.txt
}

# Check if the correct number of arguments is provided
if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <replica_id> <port_number>"
  exit 1
fi

# Extract replica ID and port number from command line arguments
replica_id="$1"
port_number="$2"

# Start the orderservice with the specified replica ID and port number
start_orderservice "$replica_id" "$port_number"

echo "Orderservice started successfully on port $port_number with replica ID $replica_id."
