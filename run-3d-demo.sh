#!/bin/bash
# JRobotics v2 - 3D Simulation Demo Launcher (Linux/macOS)
# 
# Copyright (c) 2025 Silvere Martin-Michiellot
# Licensed under the MIT License.

echo "===================================="
echo "JRobotics 3D Simulation Demo"
echo "===================================="
echo
echo "Controls:"
echo "  WASD   - Move camera"
echo "  Mouse  - Look around"
echo "  Q/Z    - Up/Down"
echo

cd "$(dirname "$0")"

if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed."
    exit 1
fi

if [ ! -d "jrobotics-robot/target/classes" ]; then
    echo "Building project..."
    mvn compile -DskipTests -q
fi

echo "Starting 3D JMonkeyEngine visualization..."
echo

mvn exec:java -pl jrobotics-robot -Dexec.mainClass="org.jrobotics.demo.Visual3DDemo" -q
