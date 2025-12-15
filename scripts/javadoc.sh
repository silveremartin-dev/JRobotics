#!/bin/bash
# JRobotics v2 - Generate JavaDoc Script (Unix/Linux/Mac)
# Copyright (c) 2025 Silvère Martin-Michiellot
# Licensed under MIT License

echo "=========================================="
echo " JRobotics v2 - Generate JavaDoc"
echo "=========================================="
echo

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "Generating JavaDoc..."
mvn javadoc:aggregate

if [ $? -ne 0 ]; then
    echo
    echo "JavaDoc generation FAILED!"
    exit 1
fi

echo
echo "=========================================="
echo " JavaDoc generated at: target/site/apidocs"
echo "=========================================="
