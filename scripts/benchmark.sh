#!/bin/bash
# JRobotics v2 - Run Benchmarks Script (Unix/Linux/Mac)
# Copyright (c) 2025 Silvère Martin-Michiellot
# Licensed under MIT License

echo "=========================================="
echo " JRobotics v2 - Run Benchmarks"
echo "=========================================="
echo

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "Running JMH benchmarks..."
mvn -pl jrobotics-benchmark exec:java

if [ $? -ne 0 ]; then
    echo
    echo "Benchmark FAILED!"
    exit 1
fi

echo
echo "=========================================="
echo " Benchmarks completed!"
echo "=========================================="
