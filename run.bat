@echo off
set "JAVA_HOME=C:\Users\Kim Eduard Saludes\.jdks\jdk-21.0.9+10"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Starting NextIT Desktop Application...
call mvnw.cmd javafx:run

