@echo off
REM JRobotics v2 - 3D Simulation Demo Launcher (Windows)
REM 
REM Copyright (c) 2025 Silvere Martin-Michiellot
REM Licensed under the MIT License.

echo ====================================
echo JRobotics 3D Simulation Demo
echo ====================================
echo.
echo Controls:
echo   WASD   - Move camera
echo   Mouse  - Look around
echo   Q/Z    - Up/Down
echo.

cd /d "%~dp0"

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven is not installed or not in PATH.
    pause
    exit /b 1
)

REM Build if needed
if not exist "jrobotics-robot\target\classes" (
    echo Building project...
    call mvn compile -DskipTests -q
)

echo Starting 3D JMonkeyEngine visualization...
echo.

call mvn exec:java -pl jrobotics-robot -Dexec.mainClass="org.jrobotics.demo.Visual3DDemo" -q

pause
