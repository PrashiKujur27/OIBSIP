#!/bin/bash
echo ""
echo " ==========================================="
echo "  ATM INTERFACE - Launcher (Mac / Linux)"
echo " ==========================================="
echo ""

if ! command -v java &> /dev/null; then
    echo " [ERROR] Java not found!"
    echo " Ubuntu/Debian : sudo apt install default-jdk"
    echo " Mac           : brew install openjdk"
    exit 1
fi

echo " Compiling..."
mkdir -p bin
javac -d bin src/ATMInterface.java

if [ $? -ne 0 ]; then
    echo " [ERROR] Compilation failed."
    exit 1
fi

echo " Launching ATM Interface..."
echo ""
java -cp bin ATMInterface
