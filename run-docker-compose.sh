#!/usr/bin/env bash
# Rebuild and start Sleep Logger API + Postgres with Docker Compose
# Run from project root: ./run-docker-compose.sh

set -e
cd "$(dirname "$0")"

echo -e "\033[33mStopping existing containers...\033[0m"
docker compose down

echo -e "\n\033[33mBuilding and starting containers...\033[0m"
docker compose up -d --build

echo -e "\n\033[32mDone. API: http://localhost:8080  Swagger: http://localhost:8080/swagger-ui.html\033[0m"
