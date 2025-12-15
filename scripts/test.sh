#!/bin/bash
# JRobotics v2 - Test Script (Unix/Linux/Mac)
# Copyright (c) 2025 Silvère Martin-Michiellot
# Licensed under MIT License

echo "=========================================="
echo " JRobotics v2 - Testing"
echo "=========================================="
echo

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "Running tests..."
mvn test

if [ $? -ne 0 ]; then
    echo
    echo "Tests FAILED!"
    exit 1
fi

echo
echo "=========================================="
echo " All tests passed!"
echo "=========================================="
