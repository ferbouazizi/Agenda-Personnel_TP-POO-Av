@echo off
REM Run script for Windows
REM Requires JDK 17+ and JavaFX SDK
REM Set JFX_PATH to your JavaFX SDK lib folder below

set JFX_PATH=C:\Program Files\javafx-sdk-21\lib
set JAR=%~dp0out\agenda_personnel.jar
set LIB=%~dp0out\lib\ojdbc8.jar

java --module-path "%JFX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp "%JAR%;%LIB%;%JFX_PATH%\*" ^
     agendatp.main
