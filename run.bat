@echo off
title ATM Interface
echo.
echo  =======================================
echo   ATM INTERFACE - Launcher (Windows)
echo  =======================================
echo.

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo  [ERROR] Java not found! Please install Java 8 or higher.
    echo  Download from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo  Compiling...
if not exist "bin" mkdir bin
javac -d bin src\ATMInterface.java

if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] Compilation failed.
    pause
    exit /b 1
)

echo  Launching ATM Interface...
echo.
java -cp bin ATMInterface
pause
