#!/bin/bash

# Check if three arguments were provided
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <number of runs> <probability> <number of iterations>"
    exit 1
fi

# Assign the provided arguments to variables
num_runs=$1
probability=$2
iterations=$3

# Base port number
base_port=12350

# The directory to store output files
OUTPUT_DIR="./output"
rm -r "$OUTPUT_DIR" 2>/dev/null # Suppress error if directory does not exist
mkdir -p "$OUTPUT_DIR"

# Loop to start the Java program the specified number of times
for ((i=1; i<=num_runs; i++))
do
    current_port=$((base_port + i - 1)) # Calculate unique port for each run
    java -jar ./target/client-0.0.1-SNAPSHOT.jar $probability $iterations --server.port=$current_port > "$OUTPUT_DIR/run_$i.txt" &
done

wait # Wait for all background processes to finish
echo "All runs completed."
