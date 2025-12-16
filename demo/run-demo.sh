#!/bin/bash
# JRobotics v2 - Demo Application Launcher (Unix/Linux/Mac)
# Copyright (c) 2025 Silvère Martin-Michiellot
# Licensed under MIT License

echo "=========================================="
echo " JRobotics v2 - Demo Application"
echo "=========================================="
echo

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Check if demo has been built
if [ ! -d "demo/target/classes" ]; then
    echo "Building demo application..."
    mvn install -DskipTests -pl jrobotics-core,jrobotics-hal,jrobotics-sensor,jrobotics-actuator,jrobotics-robot
    cd demo
    mvn compile
    cd ..
fi

echo "Running demo..."
cd demo
mvn exec:java -Dexec.mainClass="org.jrobotics.demo.RobotDemo"

echo
echo "=========================================="
