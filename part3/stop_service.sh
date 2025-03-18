#!/bin/bash

# Function to stop a replica
stop_replica() {
  local port_number=$1

  if [ -f "pid_port_mapping.txt" ]; then
    pid=$(grep "^$port_number:" pid_port_mapping.txt | cut -d':' -f2)
    if [ -n "$pid" ]; then
      echo "Stopping service on port $port_number (PID: $pid)..."
      kill "$pid"
      sed -i "/^$port_number:/d" pid_port_mapping.txt # Remove the PID from the mapping file
      echo "Service on port $port_number stopped successfully."
    else
      echo "No service running on port $port_number."
    fi
  else
    echo "No services are currently running."
  fi
}

# Call the stop_replica function with the port number passed as an argument
stop_replica "$1"
