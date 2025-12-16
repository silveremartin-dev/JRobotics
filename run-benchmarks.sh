#!/bin/bash
# JRobotics v2 - Benchmark Runner (Linux/macOS)
# 
# Copyright (c) 2025 Silvere Martin-Michiellot
# Licensed under the MIT License.

echo "===================================="
echo "JRobotics Performance Benchmarks"
echo "===================================="
echo

cd "$(dirname "$0")"

echo "Building benchmarks..."
mvn package -DskipTests -pl jrobotics-benchmark -am -q

echo
echo "Running JMH benchmarks..."
echo "This may take several minutes."
echo

java -jar jrobotics-benchmark/target/benchmarks.jar -f 1 -wi 3 -i 5
