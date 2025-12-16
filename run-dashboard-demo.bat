@echo off
REM JRobotics Dashboard Demo Launcher
REM Runs the simulated robot dashboard

cd /d "%~dp0"

echo Building JRobotics...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting Dashboard Demo...
java -cp jrobotics-demo/target/jrobotics-demo-2.0.0-SNAPSHOT.jar;jrobotics-demo/target/dependency/*;jrobotics-core/target/classes;jrobotics-sensor/target/classes;jrobotics-actuator/target/classes;jrobotics-network/target/classes;jrobotics-robot/target/classes org.jrobotics.demo.DashboardDemo

pause
