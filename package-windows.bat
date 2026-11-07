@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   NextIT Standalone Windows App Packager
echo ===================================================

if "%JAVA_HOME%"=="" (
    if exist "C:\Users\Kim Eduard Saludes\.jdks\jdk-21.0.9+10" (
        set "JAVA_HOME=C:\Users\Kim Eduard Saludes\.jdks\jdk-21.0.9+10"
    )
)

echo Using JAVA_HOME: %JAVA_HOME%
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [1/3] Compiling and packaging fat executable JAR...
call .\mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo Error: Maven package failed.
    exit /b 1
)

echo [2/3] Checking for executable jar...
if not exist "target\nextit-1.0.0-all.jar" (
    echo Error: target\nextit-1.0.0-all.jar not found!
    exit /b 1
)

echo [3/3] Creating standalone Windows application via jpackage...
if exist "dist\NextIT" rmdir /s /q "dist\NextIT"
mkdir "dist" 2>nul

call "%JAVA_HOME%\bin\jpackage.exe" ^
  --type app-image ^
  --dest dist ^
  --name NextIT ^
  --input target ^
  --main-jar nextit-1.0.0-all.jar ^
  --main-class com.nextit.AppLauncher ^
  --vendor "NextIT Team" ^
  --app-version 1.0.0

if errorlevel 1 (
    echo [Notice] jpackage app-image creation finished with code %errorlevel%.
) else (
    echo ===================================================
    echo   SUCCESS! Standalone app generated at:
    echo   dist\NextIT\NextIT.exe
    echo ===================================================
)

pause

