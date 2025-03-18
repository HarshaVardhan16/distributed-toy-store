#!/bin/bash

# Array of replica IDs
replica_ids=(1 2 3)

# Array of port numbers
port_numbers=(12348 12346 12347)

# Function to start a replica
start_replica() {
  local replica_id=$1
  local port_number=$2

  java -jar ./orderservice/target/orderservice-0.0.1-SNAPSHOT.jar --server.port=$port_number --replica.id=$replica_id --start=1 &
  # Store the PID of the Java process along with the port number
  echo "$port_number:$!" >> pid_port_mapping.txt
}
# Clear the PID-port mapping file before starting services
> pid_port_mapping.txt
# Start each replica with its corresponding ID and port number
for i in "${!replica_ids[@]}"; do
  start_replica "${replica_ids[$i]}" "${port_numbers[$i]}"
done

echo "All replicas started successfully."