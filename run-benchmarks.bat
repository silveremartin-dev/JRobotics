@echo off
REM JRobotics v2 - Benchmark Runner (Windows)
REM 
REM Copyright (c) 2025 Silvere Martin-Michiellot
REM Licensed under the MIT License.

echo ====================================
echo JRobotics Performance Benchmarks
echo ====================================
echo.

cd /d "%~dp0"

REM Build benchmark jar
echo Building benchmarks...
call mvn package -DskipTests -pl jrobotics-benchmark -am -q

if not exist "jrobotics-benchmark\target\benchmarks.jar" (
    echo Building uber JAR with shade plugin...
    call mvn package -DskipTests -pl jrobotics-benchmark -Pbenchmark -q
)

echo.
echo Running JMH benchmarks...
echo This may take several minutes.
echo.

java -jar jrobotics-benchmark\target\benchmarks.jar -f 1 -wi 3 -i 5

pause
