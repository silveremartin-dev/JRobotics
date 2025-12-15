@echo off
REM JRobotics v2 - Run Benchmarks Script (Windows)
REM Copyright (c) 2025 Silvère Martin-Michiellot
REM Licensed under MIT License

echo ==========================================
echo  JRobotics v2 - Run Benchmarks
echo ==========================================
echo.

cd /d "%~dp0.."

echo Running JMH benchmarks...
call mvn -pl jrobotics-benchmark exec:java

if %errorlevel% neq 0 (
    echo.
    echo Benchmark FAILED!
    exit /b 1
)

echo.
echo ==========================================
echo  Benchmarks completed!
echo ==========================================
