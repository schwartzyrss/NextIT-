@echo off
setlocal enabledelayedexpansion

echo ======================================================================
echo   NextIT: Track Your Progress, Build Your Future
echo   Standalone Windows Desktop Application Builder ^& Installer Packager
echo ======================================================================

:: Force current working directory to project root on the same drive
cd /d "%~dp0"
set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

if defined JAVA_HOME (
    if not exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_HOME="
    )
)

if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
    ) else if exist "C:\Program Files\Java\latest\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\latest"
    ) else if exist "%USERPROFILE%\.jdks" (
        for /d %%D in ("%USERPROFILE%\.jdks\*") do (
            if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
        )
    )
)

echo [Environment] Working Directory: %PROJECT_DIR%
echo [Environment] Using JAVA_HOME: %JAVA_HOME%
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [1/4] Compiling and packaging standalone fat executable JAR...
call .\mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo [Error] Maven package failed.
    exit /b 1
)

echo [2/4] Verifying generated executable artifacts...
if not exist "%PROJECT_DIR%\target\nextit-1.0.0-all.jar" (
    echo [Error] target\nextit-1.0.0-all.jar not found!
    exit /b 1
)

echo [3/4] Bundling standalone Windows Desktop app with JRE via jpackage...

:: 1. Prepare target\jpackage-input (Copy ONLY shaded uber-jar, excluding original-*.jar)
set "JPACKAGE_INPUT=%PROJECT_DIR%\target\jpackage-input"
if exist "%JPACKAGE_INPUT%" rmdir /s /q "%JPACKAGE_INPUT%" 2>nul
mkdir "%JPACKAGE_INPUT%" 2>nul
echo [Input] Staging standalone uber-jar to %JPACKAGE_INPUT%...
copy /y "%PROJECT_DIR%\target\nextit-1.0.0-all.jar" "%JPACKAGE_INPUT%\" >nul

:: 2. Prepare target\jpackage-temp on the same D: drive to prevent cross-drive path failures
set "JPACKAGE_TEMP=%PROJECT_DIR%\target\jpackage-temp"
if exist "%JPACKAGE_TEMP%" rmdir /s /q "%JPACKAGE_TEMP%" 2>nul
mkdir "%JPACKAGE_TEMP%" 2>nul

:: 3. Prepare clean destination directory
set "DIST_DIR=%PROJECT_DIR%\dist"
if exist "%DIST_DIR%\NextIT" rmdir /s /q "%DIST_DIR%\NextIT" 2>nul
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%" 2>nul

:: 4. Verify icon file exists (or copy/generate if needed)
set "ICON_ARG="
if exist "%PROJECT_DIR%\nextit.ico" (
    set ICON_ARG=--icon "%PROJECT_DIR%\nextit.ico"
) else if exist "%PROJECT_DIR%\src\main\resources\com\nextit\icons\nextit.ico" (
    copy /y "%PROJECT_DIR%\src\main\resources\com\nextit\icons\nextit.ico" "%PROJECT_DIR%\nextit.ico" >nul
    set ICON_ARG=--icon "%PROJECT_DIR%\nextit.ico"
) else (
    echo [Notice] Generating nextit.ico...
    call java -cp "%PROJECT_DIR%\target\classes" com.nextit.util.IconGenerator >nul 2>&1
    if exist "%PROJECT_DIR%\nextit.ico" set ICON_ARG=--icon "%PROJECT_DIR%\nextit.ico"
)

echo [jpackage] Launching jpackage on drive %~d0 with local temp directory...
call "%JAVA_HOME%\bin\jpackage.exe" ^
  --type app-image ^
  --dest "%DIST_DIR%" ^
  --temp "%JPACKAGE_TEMP%" ^
  --name NextIT ^
  !ICON_ARG! ^
  --input "%JPACKAGE_INPUT%" ^
  --main-jar nextit-1.0.0-all.jar ^
  --main-class com.nextit.AppLauncher ^
  --vendor "NextIT" ^
  --app-version 1.0.0 ^
  --description "NextIT: Track Your Progress, Build Your Future"

if errorlevel 1 (
    echo [Error] jpackage failed to generate standalone desktop application.
    exit /b 1
)

:: Clean up temporary build artifacts on the D: drive
if exist "%JPACKAGE_TEMP%" rmdir /s /q "%JPACKAGE_TEMP%" 2>nul

echo [Support] Bundling schema, seed data, and application icon into standalone package...
if exist "%PROJECT_DIR%\nextit.ico" copy /y "%PROJECT_DIR%\nextit.ico" "%DIST_DIR%\NextIT\" >nul
if exist "%PROJECT_DIR%\.env" copy /y "%PROJECT_DIR%\.env" "%DIST_DIR%\NextIT\" >nul
if exist "%PROJECT_DIR%\database" (
    xcopy /e /i /y "%PROJECT_DIR%\database" "%DIST_DIR%\NextIT\database\" >nul
)

echo [4/4] Generating Windows Installer and Desktop Shortcut launchers...

:: Generate 1-Click Desktop Shortcut Script
(
echo @echo off
echo setlocal
echo echo Creating NextIT Desktop and Start Menu shortcuts...
echo powershell -NoProfile -ExecutionPolicy Bypass -Command "$ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut([Environment]::GetFolderPath('Desktop') + '\NextIT.lnk'); $s.TargetPath = '%~dp0NextIT\NextIT.exe'; $s.WorkingDirectory = '%~dp0NextIT'; $s.IconLocation = '%~dp0NextIT\nextit.ico,0'; $s.Description = 'NextIT: Track Your Progress, Build Your Future'; $s.Save(); Write-Host '[Success] Desktop shortcut created!'"
echo pause
) > "%DIST_DIR%\Create-Desktop-Shortcut.bat"

:: Copy Full Windows Installer
if exist "%PROJECT_DIR%\package\Install-NextIT.bat" (
    copy /y "%PROJECT_DIR%\package\Install-NextIT.bat" "%DIST_DIR%\Install-NextIT.bat" >nul
)

echo.
echo ======================================================================
echo   SUCCESS! NEXTIT STANDALONE PACKAGING COMPLETE
echo ======================================================================
echo   1. Standalone Portable Folder: dist\NextIT\NextIT.exe
echo   2. Desktop Shortcut Creator:   dist\Create-Desktop-Shortcut.bat
echo   3. Full Windows Installer:     dist\Install-NextIT.bat
echo ======================================================================
