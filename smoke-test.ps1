# Smoke test: hit main API endpoints and check for 200 responses.
# Run after the API is up (e.g. .\run-docker-compose.ps1). Usage: .\smoke-test.ps1 [-BaseUrl "http://localhost:8080"]

param([string]$BaseUrl = "http://localhost:8080")

$ErrorActionPreference = "Stop"
Write-Host "Smoke testing API at $BaseUrl ..."

# 1. Create user
Write-Host -NoNewline "  POST /users ... "
try {
    $r = Invoke-RestMethod -Uri "$BaseUrl/users" -Method Post -ContentType "application/json"
    $userId = $r.id
    if (-not $userId) { throw "No id in response" }
    Write-Host "OK (user $userId)" -ForegroundColor Green
} catch {
    Write-Host "FAIL" -ForegroundColor Red
    throw
}

# 2. Create sleep log
Write-Host -NoNewline "  POST /users/$userId/sleep-logs ... "
$body = '{"sleepDate":"2025-02-22","wentToBedAt":"23:00","gotUpAt":"07:30","totalTimeInBedMinutes":510,"morningFeeling":"GOOD"}'
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/users/$userId/sleep-logs" -Method Post -Body $body -ContentType "application/json"
    Write-Host "OK" -ForegroundColor Green
} catch {
    Write-Host "FAIL" -ForegroundColor Red
    throw
}

# 3. Get last night's sleep
Write-Host -NoNewline "  GET /users/$userId/sleep-logs/last ... "
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/users/$userId/sleep-logs/last" -Method Get
    Write-Host "OK" -ForegroundColor Green
} catch {
    Write-Host "FAIL" -ForegroundColor Red
    throw
}

# 4. Get 30-day averages
Write-Host -NoNewline "  GET /users/$userId/sleep-logs/30-day-averages ... "
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/users/$userId/sleep-logs/30-day-averages" -Method Get
    Write-Host "OK" -ForegroundColor Green
} catch {
    Write-Host "FAIL" -ForegroundColor Red
    throw
}

Write-Host "All smoke tests passed." -ForegroundColor Green
