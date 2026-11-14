@echo off
setlocal enabledelayedexpansion

echo ======================================================================
echo   NextIT: Track Your Progress, Build Your Future
echo   Windows Desktop Application Installer
echo ======================================================================
echo.

set "INSTALL_DIR=%LOCALAPPDATA%\Programs\NextIT"
echo Target Installation Directory: %INSTALL_DIR%
echo.

if exist "%INSTALL_DIR%" (
    echo Cleaning previous installation...
    rmdir /s /q "%INSTALL_DIR%" 2>nul
)

mkdir "%INSTALL_DIR%" 2>nul

echo [1/3] Copying application binaries and bundled runtime...
xcopy /e /i /y "%~dp0NextIT" "%INSTALL_DIR%\" >nul
if errorlevel 1 (
    echo [Error] Failed to copy application files to %INSTALL_DIR%
    pause
    exit /b 1
)

echo [2/3] Creating Desktop and Start Menu shortcuts...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ws = New-Object -ComObject WScript.Shell; $dPath = [Environment]::GetFolderPath('Desktop') + '\NextIT.lnk'; $d = $ws.CreateShortcut($dPath); $d.TargetPath = Join-Path $env:LOCALAPPDATA 'Programs\NextIT\NextIT.exe'; $d.WorkingDirectory = Join-Path $env:LOCALAPPDATA 'Programs\NextIT'; $d.IconLocation = (Join-Path $env:LOCALAPPDATA 'Programs\NextIT\nextit.ico') + ',0'; $d.Description = 'NextIT: Track Your Progress, Build Your Future'; $d.Save(); $sm = Join-Path ([Environment]::GetFolderPath('StartMenu')) 'Programs\NextIT'; if (-not (Test-Path $sm)) { New-Item -ItemType Directory -Path $sm -Force | Out-Null }; $s = $ws.CreateShortcut((Join-Path $sm 'NextIT.lnk')); $s.TargetPath = Join-Path $env:LOCALAPPDATA 'Programs\NextIT\NextIT.exe'; $s.WorkingDirectory = Join-Path $env:LOCALAPPDATA 'Programs\NextIT'; $s.IconLocation = (Join-Path $env:LOCALAPPDATA 'Programs\NextIT\nextit.ico') + ',0'; $s.Description = 'NextIT: Track Your Progress, Build Your Future'; $s.Save(); Write-Host '[Success] Desktop and Start Menu shortcuts created!'"

echo [3/3] Registering with Windows Programs (Add or Remove Programs)...
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\NextIT" /v "DisplayName" /t REG_SZ /d "NextIT Desktop" /f >nul
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\NextIT" /v "DisplayVersion" /t REG_SZ /d "1.0.0" /f >nul
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\NextIT" /v "Publisher" /t REG_SZ /d "NextIT Team" /f >nul
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\NextIT" /v "DisplayIcon" /t REG_SZ /d "%INSTALL_DIR%\nextit.ico" /f >nul
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\NextIT" /v "UninstallString" /t REG_SZ /d "\"%INSTALL_DIR%\Uninstall-NextIT.bat\"" /f >nul

:: Create clean Uninstaller inside the installation directory
(
echo @echo off
echo setlocal
echo echo ======================================================================
echo echo   Uninstalling NextIT Desktop Application...
echo echo ======================================================================
echo powershell -NoProfile -ExecutionPolicy Bypass -Command "Remove-Item ([Environment]::GetFolderPath('Desktop') + '\NextIT.lnk') -Force -ErrorAction SilentlyContinue" 2^>nul
echo del "%%USERPROFILE%%\Desktop\NextIT.lnk" 2^>nul
echo rmdir /s /q "%%APPDATA%%\Microsoft\Windows\Start Menu\Programs\NextIT" 2^>nul
echo reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\NextIT" /f 2^>nul
echo echo Application shortcuts and registry entries removed.
echo echo To finish removing the files, delete: %INSTALL_DIR%
echo timeout /t 3 ^>nul
echo start "" cmd /c "timeout /t 1 >nul & rmdir /s /q \"%INSTALL_DIR%\""
) > "%INSTALL_DIR%\Uninstall-NextIT.bat"

echo.
echo ======================================================================
echo   SUCCESS! NextIT has been installed.
echo   You can launch NextIT from:
echo   - Desktop shortcut: NextIT
echo   - Windows Start Menu: NextIT
echo ======================================================================
echo.
pause
