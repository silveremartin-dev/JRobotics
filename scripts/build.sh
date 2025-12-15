#!/bin/bash
# JRobotics v2 - Build Script (Unix/Linux/Mac)
# Copyright (c) 2025 Silvère Martin-Michiellot
# Licensed under MIT License

echo "=========================================="
echo " JRobotics v2 - Build"
echo "=========================================="
echo

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "Building JRobotics..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo
    echo "Build FAILED!"
    exit 1
fi

echo
echo "=========================================="
echo " Build completed successfully!"
echo "=========================================="
