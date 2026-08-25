@echo off
REM ============================================================
REM  Rapid KL T250 Bus Route System - compile script
REM  Requires: JDK 11+ and the JavaFX jars inside the lib folder
REM ============================================================
cd /d "%~dp0"
if not exist out mkdir out

javac --module-path lib --add-modules javafx.controls -d out src\*.java

if %errorlevel%==0 (
    echo.
    echo [OK] Compilation successful. Run "run.bat" to start the system.
) else (
    echo.
    echo [ERROR] Compilation failed. Check the messages above.
)
pause
