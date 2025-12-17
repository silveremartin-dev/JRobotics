#!/bin/bash
# JRobotics v2 - Dashboard Demo Launcher (Linux/macOS)
#
# Copyright (c) 2025 Silvere Martin-Michiellot
# Licensed under the MIT License.

echo "===================================="
echo "JRobotics Dashboard Demo"
echo "===================================="
echo ""

cd "$(dirname "$0")"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH."
    echo "Please install Maven from https://maven.apache.org"
    exit 1
fi

echo "Starting Dashboard Server..."
echo "Access the dashboard at http://localhost:8080"
echo ""

mvn exec:java -pl jrobotics-demo -Dexec.mainClass="org.jrobotics.demo.DashboardDemo" -q
