@echo off
REM ============================================================
REM  Rapid KL T250 Bus Route System - run script (Maven)
REM  Compile first with compile.bat
REM ============================================================
cd /d "%~dp0"

where mvn >nul 2>nul
if %errorlevel%==0 (
    mvn javafx:run
) else if exist "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" (
    "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" javafx:run
) else (
    echo [ERROR] Maven not found. Install Maven or Apache NetBeans first.
)
pause
