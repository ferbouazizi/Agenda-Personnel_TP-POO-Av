#!/bin/bash
# Run script for Linux/macOS
# Usage: ./run.sh
# Requires: JDK 17+ and JavaFX SDK installed

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$SCRIPT_DIR/out/agenda_personnel.jar"
LIB="$SCRIPT_DIR/out/lib/ojdbc8.jar"

# Auto-detect JavaFX path (common locations)
JFX_PATH=""
for candidate in \
    "/usr/share/java" \
    "$HOME/javafx-sdk/lib" \
    "/opt/javafx-sdk/lib" \
    "C:/Program Files/javafx-sdk/lib"; do
    if [ -f "$candidate/javafx.controls.jar" ] || [ -f "$candidate/javafx-controls.jar" ]; then
        JFX_PATH="$candidate"
        break
    fi
done

if [ -z "$JFX_PATH" ]; then
    echo "JavaFX not found. Set JFX_PATH manually in this script."
    exit 1
fi

# Collect all JavaFX jars on this system
JFX_CP=$(find "$JFX_PATH" -name "javafx*.jar" | tr '\n' ':')

java --module-path "$JFX_PATH" \
     --add-modules javafx.controls,javafx.fxml \
     -cp "$JAR:$LIB:$JFX_CP" \
     agendatp.main
