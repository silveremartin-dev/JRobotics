@echo off
REM JRobotics v2 - Generate JavaDoc Script (Windows)
REM Copyright (c) 2025 Silvère Martin-Michiellot
REM Licensed under MIT License

echo ==========================================
echo  JRobotics v2 - Generate JavaDoc
echo ==========================================
echo.

cd /d "%~dp0.."

echo Generating JavaDoc...
call mvn javadoc:aggregate

if %errorlevel% neq 0 (
    echo.
    echo JavaDoc generation FAILED!
    exit /b 1
)

echo.
echo ==========================================
echo  JavaDoc generated at: target\site\apidocs
echo ==========================================
