param(
    [string]$JdkPath = "C:\Program Files\Java\jdk-21"
)

if (-not (Test-Path $JdkPath)) {
    Write-Host "ERROR: JDK path not found: $JdkPath" -ForegroundColor Red
    Write-Host "Please install JDK 21 and rerun the script with -JdkPath '<path>'"
    exit 1
}

# Set for current session
$env:JAVA_HOME = $JdkPath
$env:Path = "$JdkPath\bin;" + $env:Path

# Persist JAVA_HOME for future sessions (setx does not affect the current session)
setx JAVA_HOME "$JdkPath" | Out-Null

Write-Host "JAVA_HOME set for current session and persisted to user environment: $JdkPath" -ForegroundColor Green
Write-Host "Note: this script does not persistently modify PATH to avoid truncation issues with setx."
Write-Host "Run java -version and javac -version to verify the active JDK."