@echo off
REM ============================================================
REM  Rapid KL T250 Bus Route System - compile script (Maven)
REM  Requires: JDK 17+ and Maven (bundled with Apache NetBeans)
REM ============================================================
cd /d "%~dp0"

where mvn >nul 2>nul
if %errorlevel%==0 (
    mvn clean compile
) else if exist "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" (
    "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" clean compile
) else (
    echo [ERROR] Maven not found. Install Maven or Apache NetBeans first.
    pause
    exit /b 1
)

echo.
echo [OK] Compiled. Run "run.bat" to start the system.
pause
