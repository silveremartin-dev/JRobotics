@echo off
REM JRobotics v2 - Build Script (Windows)
REM Copyright (c) 2025 Silvère Martin-Michiellot
REM Licensed under MIT License

echo ==========================================
echo  JRobotics v2 - Build
echo ==========================================
echo.

cd /d "%~dp0.."

echo Building JRobotics...
call mvn clean install -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo Build FAILED!
    exit /b 1
)

echo.
echo ==========================================
echo  Build completed successfully!
echo ==========================================
