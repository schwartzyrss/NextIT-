@echo off
setlocal

REM Check if current JAVA_HOME is valid
if defined JAVA_HOME (
    if not exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_HOME="
    )
)

REM If JAVA_HOME is not set or invalid, auto-detect
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

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo Starting NextIT Desktop Application...
call mvnw.cmd javafx:run
