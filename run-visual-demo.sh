#!/bin/bash
# JRobotics v2 - Visual Simulation Demo Launcher (Linux/macOS)
# 
# Copyright (c) 2025 Silvere Martin-Michiellot
# Licensed under the MIT License.

echo "===================================="
echo "JRobotics Visual Simulation Demo"
echo "===================================="
echo

cd "$(dirname "$0")"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH."
    echo "Please install Maven from https://maven.apache.org"
    exit 1
fi

# Build if needed
if [ ! -d "jrobotics-robot/target/classes" ]; then
    echo "Building project..."
    mvn compile -DskipTests -q
    if [ $? -ne 0 ]; then
        echo "ERROR: Build failed."
        exit 1
    fi
fi

echo "Starting 2D Swing visualization..."
echo "Close the window to exit."
echo

mvn exec:java -pl jrobotics-robot -Dexec.mainClass="org.jrobotics.demo.VisualSimulationDemo" -q
