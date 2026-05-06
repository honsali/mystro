# Mystro

Mystro is a self-contained Java traditional astrology calculation engine exposed as a Spring Boot REST application.

The authoritative architecture specification is:

- [`NEW_ARCHITECTURE_SPEC.md`](NEW_ARCHITECTURE_SPEC.md)

## Current architecture

```text
REST request validation / normalization
→ Doctrine descriptive calculation, including doctrine-owned natal chart calculation
→ Doctrine predictive calculation
→ JSON response
```

A doctrine is a hardcoded Java knowledge module, not a settings profile.

## Current implementation status

Implemented now:

- Spring Boot REST API for descriptive report generation
- explicit doctrine discovery via `GET /api/doctrines`
- explicit single-doctrine descriptive generation via `POST /api/descriptive`
- doctrine-owned descriptive calculation
- shared Swiss Ephemeris-backed `BasicCalculator`
- unified `NatalChart` descriptive output
- Valens and Ptolemy descriptive doctrine calculations
- REST JSON serialization with Mystro rounding/time conventions
- thread-isolated ephemeral request logging for `/api/**`

Current descriptive reports expose top-level:

```text
engineVersion, subject, doctrine, natalChart
```

There is no top-level `basicChart` key and no top-level `descriptive` key.

## Representative natal data shape

The REST API accepts natal birth data directly in the request body:

```json
{
  "id": "ilia",
  "birthDate": "1975-07-14",
  "birthTime": "22:55:00",
  "latitude": 50.60600755996812,
  "longitude": 3.0333769552426793,
  "utcOffset": "+01:00",
  "doctrine": "valens"
}
```

## Environment

- Java 17 is required.
- Swiss Ephemeris data files under `ephe/` are required runtime data.

## Build and run

Compile:

```bash
mvn compile
```

Run tests:

```bash
mvn test
```

Start the web application:

```bash
mvn spring-boot:run
```

The server starts on port 8080 by default.

### Packaged jar

Build a standalone jar:

```bash
mvn package -DskipTests
```

Run it:

```bash
java -jar target/mystro-<version>.jar
```

The `ephe/` directory must be available from the working directory for Swiss Ephemeris calculations.

## Spring Boot API

### CORS (React frontend development)

CORS is configured for `/api/**` to allow local React frontend development.

Default allowed origins are defined in `src/main/resources/application.yml`:
- `http://localhost:5173` (Vite dev server)
- `http://localhost:3000` (Create React App)

To override allowed origins, set the `mystro.cors.allowed-origins` property:

```bash
mvn spring-boot:run -Dmystro.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

Or via environment variable:

```bash
set MYSTRO_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
mvn spring-boot:run
```

Blank or whitespace-only configured origins are ignored. If the configured list is missing or empty, Mystro falls back to the two local defaults above.

CORS does not mean data is stored; the backend remains stateless and ephemeral.

### GET /api/doctrines

List all available doctrine modules and their calculation choices.

Example request:

```bash
curl http://localhost:8080/api/doctrines
```

Response shape (direct JSON array):

```json
[
  {
    "id": "dorotheus",
      "name": "Dorotheus",
      "houseSystem": "WHOLE_SIGN",
      "zodiac": "TROPICAL",
      "terms": "EGYPTIAN",
      "triplicity": "DOROTHEAN",
      "nodeType": "MEAN"
  },
  {
    "id": "ptolemy",
    "name": "Ptolemy",
    "houseSystem": "WHOLE_SIGN",
    "zodiac": "TROPICAL",
    "terms": "PTOLEMAIC",
    "triplicity": "PTOLEMAIC",
    "nodeType": "MEAN"
  },
  {
    "id": "valens",
    "name": "Valens",
    "houseSystem": "WHOLE_SIGN",
    "zodiac": "TROPICAL",
    "terms": "EGYPTIAN",
    "triplicity": "DOROTHEAN",
    "nodeType": "MEAN"
  }
]
```

This endpoint is for discovery only; it does not select or imply a default doctrine.

### POST /api/descriptive

Generate a descriptive astrology report for one subject and one doctrine.

Each call produces exactly one report. To get reports for multiple doctrines, call this endpoint once per doctrine.

Example request:

```bash
curl -X POST http://localhost:8080/api/descriptive \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-subject",
    "birthDate": "1975-07-14",
    "birthTime": "22:55:00",
    "utcOffset": "+01:00",
    "latitude": 50.606008,
    "longitude": 3.033377,
    "doctrine": "valens"
  }'
```

Request fields:
- `id` — subject identifier (required)
- `birthDate` — local birth date as `yyyy-MM-dd` (required)
- `birthTime` — local birth time as `HH:mm:ss` (required)
- `utcOffset` — UTC offset, e.g. `+01:00` (required)
- `latitude` — geographic latitude, -90 to 90 (required)
- `longitude` — geographic longitude, -180 to 180 (required)
- `doctrine` — explicit doctrine id, e.g. `"valens"` (required)

Response shape (direct `DescriptiveAstrologyReport` object):

```json
{
  "engineVersion": "<version>",
  "subject": { ... },
  "doctrine": { ... },
  "natalChart": { ... }
}
```

Notes:
- The `doctrine` field is a single explicit id; no default doctrine is assumed.
- REST requests do not write output files; the backend is stateless.
- Responses include `Cache-Control: no-store`.
- JSON serialization uses Mystro conventions: doubles are rounded to six decimal places, dates are serialized as strings not timestamps.
- The `engineVersion` in reports comes from `mystro.engine-version` in application configuration.

### REST error responses

All REST error responses are JSON-shaped:

```json
{
  "error": "..."
}
```

Common `POST /api/descriptive` 400 cases:

- missing/empty/null request body → `{ "error": "Request body is required" }`
- missing/null/blank doctrine → `{ "error": "Doctrine id is required" }`
- unknown doctrine id → `{ "error": "Unknown doctrine: <id>" }`
- malformed JSON → `{ "error": "Malformed or missing request body" }`
- invalid latitude/longitude → `{ "error": "Latitude out of range: ..." }` or `{ "error": "Longitude out of range: ..." }`

Unexpected server errors return HTTP 500 with `{ "error": "Internal server error" }`.

## Privacy and local-first usage

The REST API is designed for privacy-friendly, local-first frontend usage:

- The backend is stateless and ephemeral: it calculates and returns, nothing is stored.
- REST descriptive calls do not write output files on the server.
- `/api/**` request logging is thread-isolated and ephemeral; request calculation logs are not retained by default.
- Responses include `Cache-Control: no-store` to discourage caching of chart data.
- The frontend should download and save report JSON locally.
- One REST call produces one report for one doctrine. To get reports for multiple doctrines, call the endpoint once per selected doctrine and save one file per doctrine.
- No authentication, database, or chart history exists on the backend.

Note: while the backend does not store data, network infrastructure may still see HTTP request/response contents. This design is privacy-friendly, not absolute anonymity.
