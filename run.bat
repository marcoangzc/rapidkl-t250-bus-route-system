@echo off
REM ============================================================
REM  Rapid KL T250 Bus Route System - run script
REM  Compile first with compile.bat
REM ============================================================
cd /d "%~dp0"
java --module-path lib --add-modules javafx.controls -cp out Main
pause
