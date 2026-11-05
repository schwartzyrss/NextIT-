$env:JAVA_HOME = "C:\Users\Kim Eduard Saludes\.jdks\jdk-21.0.9+10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "Starting NextIT Desktop Application..." -ForegroundColor Cyan
.\mvnw.cmd javafx:run

