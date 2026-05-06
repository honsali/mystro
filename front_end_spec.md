# Mystro Frontend Specification

This document is for a separate frontend agent implementing a local-first React frontend for Mystro.

Mystro is a Spring Boot REST-only traditional astrology calculation backend. The frontend should collect natal birth data, let the user choose one or more explicit doctrine modules, call the backend once per doctrine, and let the user save the returned JSON reports locally.

---

## 1. Product goal

Build a simple, local-first frontend for generating Mystro descriptive natal reports.

Primary workflow:

```text
User enters one natal subject
→ frontend fetches available doctrine choices
→ user explicitly selects doctrine(s)
→ frontend calls POST /api/descriptive once per selected doctrine
→ frontend receives direct DescriptiveAstrologyReport JSON object(s)
→ frontend lets user download/save one JSON file per doctrine locally
```

The backend does **not** persist subjects, reports, logs, sessions, or history. The frontend should not assume any backend-side storage exists.

---

## 2. Non-goals / prohibited assumptions

Do **not** implement or assume:

- authentication or user accounts
- database persistence
- backend report history
- server-side file output
- predictive endpoints
- default doctrine selection by the backend
- plural-doctrine descriptive requests
- OpenAPI/Swagger availability
- sidereal zodiac mode
- settings/profile objects not present in the API

If the UI supports multiple selected doctrines, it must loop over single-doctrine requests.

---

## 3. Backend base URL

Default local backend:

```text
http://localhost:8080
```

Frontend implementation should centralize this in config, e.g.:

```ts
const API_BASE_URL = import.meta.env.VITE_MYSTRO_API_BASE_URL ?? 'http://localhost:8080';
```

Recommended frontend dev origins already allowed by backend CORS:

- `http://localhost:5173` — Vite
- `http://localhost:3000` — Create React App

---

## 4. REST API contract

### 4.1 `GET /api/doctrines`

Lists available hardcoded doctrine modules.

Request:

```http
GET /api/doctrines
```

Response: direct JSON array.

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
  }
]
```

Important:

- This endpoint is discovery only.
- It does not choose a default doctrine.
- The frontend must send the selected doctrine id in each descriptive request.

Current known doctrine ids:

- `dorotheus`
- `ptolemy`
- `valens`

Do not hardcode this list as authoritative; load it from the endpoint.

---

### 4.2 `POST /api/descriptive`

Generates one descriptive report for one natal subject and one doctrine.

Request:

```http
POST /api/descriptive
Content-Type: application/json
```

Body:

```json
{
  "id": "ilia",
  "birthDate": "1975-07-14",
  "birthTime": "22:55:00",
  "utcOffset": "+01:00",
  "latitude": 50.606008,
  "longitude": 3.033377,
  "doctrine": "valens"
}
```

Response: direct `DescriptiveAstrologyReport` JSON object.

```json
{
  "engineVersion": "<version>",
  "subject": {
    "id": "ilia",
    "localBirthDateTime": "1975-07-14T22:55:00+01:00",
    "resolvedUtcInstant": "1975-07-14T21:55:00Z",
    "latitude": 50.606008,
    "longitude": 3.033377
  },
  "doctrine": {
    "id": "valens",
    "name": "Valens",
    "houseSystem": "WHOLE_SIGN",
    "zodiac": "TROPICAL",
    "terms": "EGYPTIAN",
    "triplicity": "DOROTHEAN",
    "nodeType": "MEAN"
  },
  "natalChart": {}
}
```

Important current shape:

- There is **no** `{ "report": ... }` wrapper.
- There is **no** `suggestedFilename` field.
- The frontend should generate filenames locally.
- The report object itself has top-level:
  - `engineVersion`
  - `subject`
  - `doctrine`
  - `natalChart`

Response headers:

- `Cache-Control: no-store` on descriptive success/error responses.

---

## 5. Request validation rules

Frontend should validate before sending, but backend remains authoritative.

Required request fields:

| Field | Type | Format / range | Notes |
|---|---:|---|---|
| `id` | string | non-blank | Subject/report identifier. Use for local filename. |
| `birthDate` | string | `yyyy-MM-dd` | Local civil birth date. |
| `birthTime` | string | `HH:mm:ss` | Must include seconds. |
| `utcOffset` | string | e.g. `+01:00`, `-05:00`, `Z` if supported by Java `ZoneOffset` | Do not infer silently from browser. |
| `latitude` | number | `-90` to `90` | Decimal degrees. North positive, south negative. |
| `longitude` | number | `-180` to `180` | Decimal degrees. East positive, west negative. |
| `doctrine` | string | must match one returned doctrine id | Required; no hidden default. |

Recommended UI behavior:

- Use a date input for `birthDate`.
- Use a time input with seconds if possible; otherwise append `:00` only if the user explicitly accepts minute precision.
- Provide a UTC offset input separate from local time.
- Do not auto-use the user's browser timezone as the birth timezone.
- Latitude/longitude should be explicit decimal fields.

---

## 6. Error contract

Error responses are JSON-shaped:

```ts
interface ErrorResponse {
  error: string;
}
```

Common `400` errors from `POST /api/descriptive`:

```json
{ "error": "Request body is required" }
{ "error": "Doctrine id is required" }
{ "error": "Unknown doctrine: valensx" }
{ "error": "Malformed or missing request body" }
{ "error": "Latitude out of range: 123" }
{ "error": "Longitude out of range: 200" }
```

Unexpected errors return HTTP `500`:

```json
{ "error": "Internal server error" }
```

Frontend error handling requirements:

- If `response.ok === false`, attempt to parse `{ error }`.
- If parsing fails, show a generic HTTP error.
- Do not expose stack traces; backend should not send them.
- Show validation errors near the form when possible.
- For multi-doctrine generation, show per-doctrine success/failure.

---

## 7. TypeScript model

Use these as frontend starting types. Treat `natalChart` as extensible; backend may add doctrine-specific fields.

```ts
export interface DoctrineInfo {
  id: string;
  name: string;
  houseSystem: 'WHOLE_SIGN' | 'PLACIDUS' | string;
  zodiac: 'TROPICAL' | string;
  terms: 'EGYPTIAN' | 'PTOLEMAIC' | string;
  triplicity: 'DOROTHEAN' | 'PTOLEMAIC' | string;
  nodeType: 'MEAN' | string;
}

export interface DescriptiveRequest {
  id: string;
  birthDate: string;   // yyyy-MM-dd
  birthTime: string;   // HH:mm:ss
  utcOffset: string;   // e.g. +01:00
  latitude: number;
  longitude: number;
  doctrine: string;
}

export interface SubjectSummary {
  id: string;
  localBirthDateTime: string;
  resolvedUtcInstant: string;
  latitude: number;
  longitude: number;
}

export interface DescriptiveAstrologyReport {
  engineVersion: string;
  subject: SubjectSummary;
  doctrine: DoctrineInfo;
  natalChart: NatalChart;
}

export interface ErrorResponse {
  error: string;
}

export interface NatalChart {
  resolvedUtcInstant?: string;
  julianDayUt?: number;
  julianDayTt?: number;
  deltaTSeconds?: number;
  armc?: number;
  localApparentSiderealTimeHours?: number;
  trueObliquity?: number;
  meanObliquity?: number;
  nutationLongitude?: number;
  nutationObliquity?: number;
  points?: Record<string, ChartPoint>;
  houses?: House[];
  pairwiseRelations?: PairwiseRelation[];
  moonPhase?: Record<string, unknown>;
  sect?: Record<string, unknown>;
  syzygy?: Record<string, unknown>;
  lots?: Record<string, unknown>;
  [key: string]: unknown;
}

export interface ChartPoint {
  longitude?: number;
  sign?: string;
  degreeInSign?: number;
  latitude?: number;
  rightAscension?: number;
  declination?: number;
  altitude?: number;
  aboveHorizon?: boolean;
  speed?: number;
  meanDailySpeed?: number;
  speedRatio?: number;
  retrograde?: boolean;
  house?: number;
  wholeSignHouse?: number;
  quadrantHouse?: number | null;
  angularity?: string;
  dignities?: string[];
  debilities?: string[];
  solarPhase?: string;
  solarCondition?: string;
  sect?: Record<string, unknown>;
  type?: 'PLANET' | 'ANGLE' | string;
  [key: string]: unknown;
}

export interface House {
  house: number;
  cuspLongitude: number;
  sign: string;
  degreeInSign: number;
}

export interface PairwiseRelation {
  pointAName: string;
  pointBName: string;
  ecliptic?: {
    angularSeparation?: number;
    signDistance?: number;
  };
  equatorial?: {
    declinationDifference?: number;
    contraParallelSeparation?: number;
    sameHemisphere?: boolean;
  };
  aspect?: {
    type: string;
    orbFromExact?: number;
  };
  [key: string]: unknown;
}
```

---

## 8. API client requirements

Create a small API client layer. Do not scatter `fetch` calls through UI components.

Suggested module:

```text
src/api/mystroClient.ts
```

Required functions:

```ts
async function listDoctrines(): Promise<DoctrineInfo[]>;
async function generateDescriptiveReport(request: DescriptiveRequest): Promise<DescriptiveAstrologyReport>;
```

Recommended implementation details:

```ts
async function parseError(response: Response): Promise<Error> {
  try {
    const body = (await response.json()) as Partial<ErrorResponse>;
    return new Error(body.error || `HTTP ${response.status}`);
  } catch {
    return new Error(`HTTP ${response.status}`);
  }
}

export async function listDoctrines(): Promise<DoctrineInfo[]> {
  const response = await fetch(`${API_BASE_URL}/api/doctrines`, {
    method: 'GET',
    cache: 'no-store',
  });
  if (!response.ok) throw await parseError(response);
  return response.json();
}

export async function generateDescriptiveReport(
  request: DescriptiveRequest,
): Promise<DescriptiveAstrologyReport> {
  const response = await fetch(`${API_BASE_URL}/api/descriptive`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    cache: 'no-store',
    body: JSON.stringify(request),
  });
  if (!response.ok) throw await parseError(response);
  return response.json();
}
```

---

## 9. Filename and local download

Because the backend no longer returns `suggestedFilename`, the frontend owns filename generation.

Recommended filename:

```text
{safeSubjectId}-{safeDoctrineId}-descriptive.json
```

Example:

```text
ilia-valens-descriptive.json
```

Implement a safe filename helper:

```ts
export function safeFilePart(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9._-]+/g, '-')
    .replace(/^-+|-+$/g, '') || 'subject';
}

export function reportFilename(request: Pick<DescriptiveRequest, 'id' | 'doctrine'>): string {
  return `${safeFilePart(request.id)}-${safeFilePart(request.doctrine)}-descriptive.json`;
}
```

Download helper:

```ts
export function downloadJson(filename: string, data: unknown): void {
  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: 'application/json',
  });
  const url = URL.createObjectURL(blob);
  try {
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
  } finally {
    URL.revokeObjectURL(url);
  }
}
```

---

## 10. Required UI behavior

Minimum UI:

1. Backend status / connection indicator
   - Load doctrines on page open.
   - If loading fails, show a clear backend connection error.
2. Subject form
   - subject id
   - birth date
   - birth time including seconds
   - UTC offset
   - latitude
   - longitude
3. Doctrine selector
   - list doctrines from `GET /api/doctrines`
   - allow one or more selected doctrines
   - do not submit if none selected
4. Generate action
   - for each selected doctrine, send one `POST /api/descriptive`
   - show loading state
   - prevent duplicate accidental submissions or handle them gracefully
5. Results area
   - show generated report metadata: subject id, doctrine, engine version
   - provide download button per report
   - optionally provide preview/copy raw JSON
6. Error area
   - show request validation errors
   - show backend `{ error }` message
   - if multiple doctrines are selected, show which doctrine failed

Recommended UX details:

- Do not silently preselect a doctrine. If you choose to visually suggest one, require explicit user confirmation before submission.
- Preserve user-entered form values in component state. Local browser storage is optional, but if used, make it clear it is local only.
- For multiple doctrines, execute sequentially or with limited concurrency. Sequential is simpler and easier to explain.

---

## 11. Suggested React structure

A simple Vite + React + TypeScript structure is sufficient.

```text
src/
  api/
    mystroClient.ts
  components/
    BackendStatus.tsx
    SubjectForm.tsx
    DoctrineSelector.tsx
    ReportResults.tsx
    ErrorBanner.tsx
  domain/
    mystroTypes.ts
    filename.ts
    validation.ts
  App.tsx
  main.tsx
```

Keep domain/API concerns separate from presentational components.

---

## 12. Frontend validation recommendations

Create a validation function returning field-specific messages.

Rules:

- `id.trim()` required
- `birthDate` required and must match `YYYY-MM-DD`
- `birthTime` required and must match `HH:mm:ss`
- `utcOffset` required and should match `Z` or `[+-]HH:mm`
- `latitude` required numeric in `[-90, 90]`
- `longitude` required numeric in `[-180, 180]`
- at least one doctrine selected

Do not over-normalize birth data. The backend should receive exactly what the user intentionally entered.

---

## 13. Report preview guidance

The first version does not need a full chart renderer.

Acceptable preview:

- show `engineVersion`
- show subject summary
- show doctrine summary
- show key chart metadata if present:
  - resolved UTC instant
  - sect
  - houses count
  - point names
- show collapsible raw JSON preview
- provide download button

Do not attempt to reinterpret astrology calculations in the frontend. The backend owns calculations and doctrine logic.

---

## 14. Privacy and data handling

Mystro is intended to be local-first.

Frontend requirements:

- Do not send report data anywhere except the configured Mystro backend.
- Do not add analytics, telemetry, or third-party logging without explicit approval.
- Do not persist reports to a remote service.
- If using browser local storage, document that it is local browser persistence and allow clearing it.
- Prefer direct file download for report saving.

Backend behavior to reflect in UI copy:

- backend does not store reports
- backend does not write report files
- request calculation logs are ephemeral and not exposed to the frontend

---

## 15. Testing expectations

Suggested frontend tests:

### Unit tests

- request validation
- filename sanitization
- API error parsing

### Component tests

- doctrine selector renders loaded doctrine list
- form blocks submit when required fields are missing
- successful generation shows report and download action
- backend error displays `{ error }` message

### API mocking

Use MSW or equivalent to mock:

- `GET /api/doctrines` returns direct array
- `POST /api/descriptive` returns direct report object
- `POST /api/descriptive` returns `400 { error: ... }`

Important: mocks must use the current direct response shapes, not old wrapper shapes.

---

## 16. Manual acceptance checklist

A frontend implementation is acceptable when:

- The app starts locally and connects to `http://localhost:8080` by default.
- Doctrines load from `GET /api/doctrines` and display as selectable options.
- The user cannot generate a report without explicitly selecting at least one doctrine.
- `POST /api/descriptive` sends exactly one doctrine id per request.
- The frontend correctly handles the direct `DescriptiveAstrologyReport` response.
- The frontend does not expect `report`, `doctrines`, or `suggestedFilename` wrappers.
- The user can download one JSON report file per doctrine.
- Backend error responses display cleanly.
- No backend persistence or server-side report files are assumed.

---

## 17. Example end-to-end frontend flow

```ts
const doctrines = await listDoctrines();

const subject = {
  id: 'ilia',
  birthDate: '1975-07-14',
  birthTime: '22:55:00',
  utcOffset: '+01:00',
  latitude: 50.606008,
  longitude: 3.033377,
};

const selectedDoctrineIds = ['valens', 'ptolemy'];

for (const doctrine of selectedDoctrineIds) {
  const request = { ...subject, doctrine };
  const report = await generateDescriptiveReport(request);
  downloadJson(reportFilename(request), report);
}
```

---

## 18. Backend facts the frontend should not hide

Show or document these where useful:

- Mystro currently supports descriptive natal reports only.
- Predictive reports are not implemented as REST endpoints yet.
- Doctrine modules differ; absent concepts are absent from a doctrine's report.
- Dorotheus exists as a doctrine choice but currently has no doctrine-poured descriptive sections yet.
- Output numbers are rounded by backend JSON serialization.
- `engineVersion` is informational and should be displayed/read from the report, not computed by the frontend.
