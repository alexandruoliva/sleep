# Rebuild and start Sleep Logger API + Postgres with Docker Compose
# Run from project root: .\run-docker-compose.ps1

Set-Location $PSScriptRoot

Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose down

Write-Host "`nBuilding and starting containers..." -ForegroundColor Yellow
docker-compose up -d --build

Write-Host "`nDone. API: http://localhost:8080  Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor Green
