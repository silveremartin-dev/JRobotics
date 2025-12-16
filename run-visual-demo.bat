@echo off
REM JRobotics v2 - Visual Simulation Demo Launcher (Windows)
REM 
REM Copyright (c) 2025 Silvere Martin-Michiellot
REM Licensed under the MIT License.

echo ====================================
echo JRobotics Visual Simulation Demo
echo ====================================
echo.

cd /d "%~dp0"

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven is not installed or not in PATH.
    echo Please install Maven from https://maven.apache.org
    pause
    exit /b 1
)

REM Build if needed
if not exist "jrobotics-robot\target\classes" (
    echo Building project...
    call mvn compile -DskipTests -q
    if %ERRORLEVEL% neq 0 (
        echo ERROR: Build failed.
        pause
        exit /b 1
    )
)

echo Starting 2D Swing visualization...
echo Close the window to exit.
echo.

call mvn exec:java -pl jrobotics-robot -Dexec.mainClass="org.jrobotics.demo.VisualSimulationDemo" -q

pause
