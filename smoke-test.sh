#!/usr/bin/env bash
# Smoke test: hit main API endpoints and check for 200 responses.
# Run after the API is up (e.g. ./run-docker-compose.sh). Usage: ./smoke-test.sh [BASE_URL]

set -e
BASE_URL="${1:-http://localhost:8080}"

echo "Smoke testing API at $BASE_URL ..."

# 1. Create user
echo -n "  POST /users ... "
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/users" -H "Content-Type: application/json")
HTTP_BODY=$(echo "$RESP" | sed '$d')
HTTP_CODE=$(echo "$RESP" | tail -n 1)
if [ "$HTTP_CODE" != "200" ]; then
  echo "FAIL (HTTP $HTTP_CODE)"
  echo "$HTTP_BODY"
  exit 1
fi
USER_ID=$(echo "$HTTP_BODY" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
if [ -z "$USER_ID" ]; then
  echo "FAIL (no id in response)"
  exit 1
fi
echo "OK (user $USER_ID)"

# 2. Create sleep log
echo -n "  POST /users/$USER_ID/sleep-logs ... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/users/$USER_ID/sleep-logs" \
  -H "Content-Type: application/json" \
  -d '{"sleepDate":"2025-02-22","wentToBedAt":"23:00","gotUpAt":"07:30","totalTimeInBedMinutes":510,"morningFeeling":"GOOD"}')
if [ "$HTTP_CODE" != "200" ]; then
  echo "FAIL (HTTP $HTTP_CODE)"
  exit 1
fi
echo "OK"

# 3. Get last night's sleep
echo -n "  GET /users/$USER_ID/sleep-logs/last ... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/$USER_ID/sleep-logs/last")
if [ "$HTTP_CODE" != "200" ]; then
  echo "FAIL (HTTP $HTTP_CODE)"
  exit 1
fi
echo "OK"

# 4. Get 30-day averages
echo -n "  GET /users/$USER_ID/sleep-logs/30-day-averages ... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/$USER_ID/sleep-logs/30-day-averages")
if [ "$HTTP_CODE" != "200" ]; then
  echo "FAIL (HTTP $HTTP_CODE)"
  exit 1
fi
echo "OK"

echo "All smoke tests passed."
