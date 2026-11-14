# Check if JAVA_HOME is already set and valid
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    if (Test-Path "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2.1"
    } elseif (Test-Path "C:\Program Files\Java\latest\bin\java.exe") {
        $env:JAVA_HOME = "C:\Program Files\Java\latest"
    } elseif (Test-Path "$env:USERPROFILE\.jdks") {
        $found = Get-ChildItem -Path "$env:USERPROFILE\.jdks" -Directory | Where-Object { Test-Path "$($_.FullName)\bin\java.exe" } | Select-Object -First 1
        if ($found) {
            $env:JAVA_HOME = $found.FullName
        }
    }
}

if ($env:JAVA_HOME) {
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}

Write-Host "Starting NextIT Desktop Application..." -ForegroundColor Cyan
.\mvnw.cmd javafx:run
