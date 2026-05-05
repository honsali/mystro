# Mystro

Mystro is a self-contained Java traditional astrology calculation engine.

The authoritative architecture specification is:

- [`NEW_ARCHITECTURE_SPEC.md`](NEW_ARCHITECTURE_SPEC.md)

## Current architecture

```text
Input loading
→ Input validation / normalization
→ Doctrine descriptive calculation, including doctrine-owned natal chart calculation
→ Doctrine predictive calculation
→ Formatting / printing
```

Current output families:

```text
Natal data × Doctrine modules → descriptive output
Natal data × Doctrine modules × inquiry periods → predictive output
```

A doctrine is a hardcoded Java knowledge module, not a settings profile.

## Current implementation status

Implemented now:

- natal input loading from `input/subject-list.json`
- explicit CLI subject/doctrine selection
- doctrine-owned descriptive calculation
- shared Swiss Ephemeris-backed `BasicCalculator`
- unified `NatalChart` descriptive output
- Valens and Ptolemy descriptive doctrine calculations
- JSON report writing
- run logging
- Spring Boot REST API for descriptive report generation

Current descriptive reports expose top-level:

```text
engineVersion, subject, doctrine, natalChart
```

There is no top-level `basicChart` key and no top-level `descriptive` key.

## Input

`input/subject-list.json` contains natal data only.

Current natal entry shape:

```json
{
  "id": "ilia",
  "birthDate": "1975-07-14",
  "birthTime": "22:55:00",
  "latitude": 50.60600755996812,
  "longitude": 3.0333769552426793,
  "utcOffset": "+01:00"
}
```

## Environment

- Java 17 is required.
- Swiss Ephemeris data files under `ephe/` are required runtime data.

## CLI

Build:

```bash
mvn compile
```

Run a subject with explicit doctrine modules:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

No hidden default doctrine should be introduced.

## Spring Boot API

Start the web application:

```bash
mvn spring-boot:run
```

The server starts on port 8080 by default.

### CORS (React frontend development)

CORS is configured for `/api/**` to allow local React frontend development.

Default allowed origins:
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

CORS does not mean data is stored; the backend remains stateless and ephemeral.

### GET /api/doctrines

List all available doctrine modules and their calculation choices.

Example request:

```bash
curl http://localhost:8080/api/doctrines
```

Response shape:

```json
{
  "doctrines": [
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
}
```

This endpoint is for discovery only; it does not select or imply a default doctrine.

### POST /api/descriptive

Generate a descriptive astrology report for one subject and one doctrine.

Each call produces exactly one report. To get reports for multiple doctrines, call this endpoint once per doctrine. The response includes a `suggestedFilename` hint for local download.

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

Response shape:

```json
{
  "report": {
    "engineVersion": "1.0.0",
    "subject": { ... },
    "doctrine": { ... },
    "natalChart": { ... }
  },
  "suggestedFilename": "test-subject-valens-descriptive.json"
}
```

Notes:
- The `doctrine` field is a single explicit id; no default doctrine is assumed.
- The `suggestedFilename` is a hint for the frontend to use when saving the report locally.
- REST requests do not write output files; the backend is stateless.
- Responses include `Cache-Control: no-store`.
- JSON serialization uses the same conventions as file output: doubles are rounded to six decimal places, dates are serialized as strings not timestamps.
- The `engineVersion` in reports is resolved from Maven metadata embedded in the jar, so it is correct even when running from a packaged jar without `pom.xml`.

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

Unexpected server errors return HTTP 500 with `{ "error": "Internal server error" }` (stack trace logged server-side only).

### Packaged jar

Build a standalone jar:

```bash
mvn package -DskipTests
```

Run it:

```bash
java -jar target/mystro-1.0.0.jar
```

The `ephe/` directory must be available from the working directory for Swiss Ephemeris calculations.

### Privacy and local-first usage

The REST API is designed for privacy-friendly, local-first frontend usage:

- The backend is stateless and ephemeral: it calculates and returns, nothing is stored.
- REST descriptive calls do not write output files on the server.
- REST calculation logging is thread-isolated and ephemeral; request calculation logs are not retained by default.
- Responses include `Cache-Control: no-store` to discourage caching of chart data.
- The frontend should download and save report JSON locally using the `suggestedFilename`.
- One REST call produces one report for one doctrine. To get reports for multiple doctrines, call the endpoint once per selected doctrine and save one file per doctrine.
- No authentication, database, or chart history exists on the backend.

Note: while the backend does not store data, network infrastructure (proxies, load balancers) may still see HTTP request/response contents. This design is privacy-friendly, not absolute anonymity.

## Current output

```text
output/{subjectId}/{doctrineId}-descriptive.json
output/run-logger.json
```

Target predictive output path:

```text
output/{subjectId}/{doctrineId}-predictive.json
```
