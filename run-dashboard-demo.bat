@echo off
REM JRobotics v2 - Dashboard Demo Launcher (Windows)
REM 
REM Copyright (c) 2025 Silvere Martin-Michiellot
REM Licensed under the MIT License.

echo ====================================
echo JRobotics Dashboard Demo
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

echo Starting Dashboard Server...
echo Access the dashboard at http://localhost:8080
echo.

call mvn exec:java -pl jrobotics-demo -Dexec.mainClass="org.jrobotics.demo.DashboardDemo" -q

pause
