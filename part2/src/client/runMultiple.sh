#!/bin/bash

# Check if an argument was provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <number of runs>"
    exit 1
fi

# The directory to store output files
OUTPUT_DIR="./output"
rm -r "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Loop to start the Java program the specified number of times
for ((i=1; i<=$1; i++))
do
    java -cp "./target/client-1.0-SNAPSHOT-jar-with-dependencies.jar"  com.client.Client > "$OUTPUT_DIR/run_$i.txt" &
done

wait # Wait for all background processes to finish
echo "All runs completed."