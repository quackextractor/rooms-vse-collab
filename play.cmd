@echo off
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo Java is not installed, or not on PATH.
    echo Install a JDK from https://adoptium.net and run this again.
    pause
    exit /b 1
)

java -jar rooms.jar
pause