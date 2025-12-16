@echo off
REM JRobotics v2 - Demo Application Launcher (Windows)
REM Copyright (c) 2025 Silvère Martin-Michiellot
REM Licensed under MIT License

echo ==========================================
echo  JRobotics v2 - Demo Application
echo ==========================================
echo.

cd /d "%~dp0.."

REM Check if demo has been built
if not exist "demo\target\classes" (
    echo Building demo application...
    call mvn install -DskipTests -pl jrobotics-core,jrobotics-hal,jrobotics-sensor,jrobotics-actuator,jrobotics-robot
    cd demo
    call mvn compile
    cd ..
)

echo Running demo...
cd demo
call mvn exec:java -Dexec.mainClass="org.jrobotics.demo.RobotDemo"

echo.
echo ==========================================
pause
