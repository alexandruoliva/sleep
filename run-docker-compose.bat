@echo off
REM Rebuild and start Sleep Logger API + Postgres with Docker Compose
REM Run from project root (folder containing docker-compose.yml)

cd /d "%~dp0"

echo Stopping existing containers...
docker-compose down

echo.
echo Building and starting containers...
docker-compose up -d --build

echo.
echo Done. API: http://localhost:8080  Swagger: http://localhost:8080/swagger-ui.html
pause
