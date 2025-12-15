@echo off
REM JRobotics v2 - Test Script (Windows)
REM Copyright (c) 2025 Silvère Martin-Michiellot
REM Licensed under MIT License

echo ==========================================
echo  JRobotics v2 - Testing
echo ==========================================
echo.

cd /d "%~dp0.."

echo Running tests...
call mvn test

if %errorlevel% neq 0 (
    echo.
    echo Tests FAILED!
    exit /b 1
)

echo.
echo ==========================================
echo  All tests passed!
echo ==========================================
