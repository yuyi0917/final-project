param(
    [string]$Root = (Get-Location).Path
)

# Choose javac from JAVA_HOME if available, otherwise rely on PATH
$javac = "javac"
if ($env:JAVA_HOME) {
    $maybe = Join-Path $env:JAVA_HOME "bin\javac.exe"
    if (Test-Path $maybe) { $javac = $maybe }
}

Write-Host "Using javac: $javac" -ForegroundColor Cyan

# Find Main.java files and compile each directory containing them
$files = Get-ChildItem -Path $Root -Recurse -Filter Main.java -ErrorAction SilentlyContinue
if (-not $files) {
    Write-Host "No Main.java files found under $Root" -ForegroundColor Yellow
    exit 0
}

foreach ($f in $files) {
    $dir = $f.Directory.FullName
    Write-Host "\n--- Compiling in: $dir ---" -ForegroundColor Green
    Push-Location $dir
    & $javac *.java 2>&1 | ForEach-Object { Write-Host $_ }
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Compilation returned exit code $LASTEXITCODE" -ForegroundColor Red
    } else {
        Write-Host "Compilation succeeded in $dir" -ForegroundColor Green
    }
    Pop-Location
}

Write-Host "\nDone. Use `java -cp <dir> Main` to run a compiled Main class." -ForegroundColor Cyan