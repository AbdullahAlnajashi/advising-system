# Compiles the sources and tests, then runs the full test suite (Windows PowerShell).
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (Test-Path out) { Remove-Item -Recurse -Force out }
$files = Get-ChildItem -Recurse -Filter *.java -Path src, test | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp out AllTests
exit $LASTEXITCODE
