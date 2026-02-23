# Sleep Logger API

REST API for logging and querying sleep data. Supports creating/fetching the last night's sleep log and 30-day averages. The API is user-aware; authentication/authorization is out of scope.

## How to run

- **Docker (recommended):** From the project root:
  ```bash
  docker-compose up --build
  ```
  Requires Docker; uses ports **5432** (PostgreSQL) and **8080** (API).

- **Reset database (e.g. after schema/migration changes):** Run `.\reset-db.ps1` (Windows PowerShell) to stop containers, remove Postgres data, and start fresh.

- **Local run:** Start PostgreSQL (port 5432) with the same credentials as in `application.properties`, then:
  ```bash
  ./gradlew bootRun
  ```

- **API base:** `http://localhost:8080`  
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

## How to test

1. **Unit tests**
   ```bash
   ./gradlew test
   ```
   Or on Windows: `.\gradlew.bat test`

2. **Manual API testing**
   - Import `Sleep-Logger-API.postman_collection.json` into Postman.
   - Run **Create user** first; the collection saves the returned `id` (UUID) as `userId`.
   - Then use **Create or update sleep log**, **Get last night's sleep**, and **Get 30-day averages** with that user.

## API overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/users` | Create a user; returns `{ "id": "<uuid>" }`. |
| POST | `/users/{userId}/sleep-logs` | Create or update sleep log for the given date (body: sleepDate, wentToBedAt, gotUpAt, totalTimeInBedMinutes, morningFeeling). |
| GET | `/users/{userId}/sleep-logs/last` | Get the most recent sleep log for the user. |
| GET | `/users/{userId}/sleep-logs/30-day-averages` | Get 30-day averages: range, average time in bed, average bed/rise times, morning feeling frequencies. |

**Morning feeling:** one of `BAD`, `OK`, `GOOD`.

## Error responses

- **400 Bad Request** – Request validation failed (e.g. missing or invalid body fields, invalid `morningFeeling`, `totalTimeInBedMinutes` outside 1–1440, or invalid `userId` UUID in the path). The response body is typically Spring’s default validation error format (field errors).
- **404 Not Found** – User does not exist (for sleep-log and 30-day-averages endpoints), or no sleep log exists for “last night” (for GET last). Response body may be empty.

No specific error payload format is guaranteed; clients should rely on HTTP status codes.
