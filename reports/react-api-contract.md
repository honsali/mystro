# React Frontend API Contract

This document defines the Mystro REST API contract for React frontend integration. The backend is stateless and ephemeral; all report data should be stored locally by the frontend.

## Base URL

```
http://localhost:8080
```

## CORS

CORS is configured for `/api/**` with default allowed origins:
- `http://localhost:5173` (Vite)
- `http://localhost:3000` (CRA)

Override via `mystro.cors.allowed-origins` property or `MYSTRO_CORS_ALLOWED_ORIGINS` environment variable.

## Endpoints

### GET /api/doctrines

List available doctrine modules and their calculation choices.

### POST /api/descriptive

Generate one descriptive report for one subject and one doctrine.

- One request = one report = one doctrine.
- No output files are written on the server.
- Response includes `Cache-Control: no-store`.
- Response includes `suggestedFilename` for local download.

## TypeScript Interfaces

```typescript
// --- Request ---

interface DescriptiveRequest {
  id: string;
  birthDate: string;       // yyyy-MM-dd
  birthTime: string;       // HH:mm:ss
  utcOffset: string;       // e.g. "+01:00"
  latitude: number;        // -90 to 90
  longitude: number;       // -180 to 180
  doctrine: string;        // e.g. "valens"
}

// --- Response ---

interface DescriptiveResponse {
  report: DescriptiveAstrologyReport;
  suggestedFilename: string;  // e.g. "ilia-valens-descriptive.json"
}

interface DescriptiveAstrologyReport {
  engineVersion: string;
  subject: Subject;
  doctrine: DoctrineSummary;
  natalChart: NatalChart;     // large object, see backend output for full shape
}

interface Subject {
  id: string;
  localBirthDateTime: string;
  resolvedUtcInstant: string;
  latitude: number;
  longitude: number;
}

interface DoctrineSummary {
  id: string;
  name: string;
  houseSystem: string;
  zodiac: string;
  terms: string;
  triplicity: string;
  nodeType: string;
}

// NatalChart contains points, houses, pairwise relations, etc.
// Use `unknown` or a dedicated type if building a full viewer.
type NatalChart = Record<string, unknown>;

// --- Doctrine discovery ---

interface DoctrinesResponse {
  doctrines: DoctrineInfo[];
}

interface DoctrineInfo {
  id: string;
  name: string;
  houseSystem: string;
  zodiac: string;
  terms: string;
  triplicity: string;
  nodeType: string;
}

// --- Error ---

interface ErrorResponse {
  error: string;
}
```

## Fetch Examples

### List doctrines

```typescript
const response = await fetch('http://localhost:8080/api/doctrines');
const data: DoctrinesResponse = await response.json();
// data.doctrines[0].id === "dorotheus"
```

### Generate and download a descriptive report

```typescript
async function fetchAndDownload(request: DescriptiveRequest): Promise<void> {
  const response = await fetch('http://localhost:8080/api/descriptive', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error: ErrorResponse = await response.json();
    throw new Error(error.error);
  }

  const data: DescriptiveResponse = await response.json();

  // Download as local file using suggestedFilename
  const blob = new Blob([JSON.stringify(data.report, null, 2)], {
    type: 'application/json',
  });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = data.suggestedFilename;
  a.click();
  URL.revokeObjectURL(url);
}
```

### Multiple doctrines: one file per doctrine

```typescript
async function fetchMultipleDoctrines(
  subject: Omit<DescriptiveRequest, 'doctrine'>,
  doctrineIds: string[]
): Promise<void> {
  for (const doctrine of doctrineIds) {
    await fetchAndDownload({ ...subject, doctrine });
  }
}

// Usage:
// await fetchMultipleDoctrines(
//   { id: 'ilia', birthDate: '1975-07-14', birthTime: '22:55:00',
//     utcOffset: '+01:00', latitude: 50.606008, longitude: 3.033377 },
//   ['valens', 'ptolemy']
// );
```

## Notes

- Reports should be stored locally by the frontend; the backend does not persist anything.
- `Cache-Control: no-store` is sent on descriptive responses to discourage caching.
- Doctrine ids are explicit; no default doctrine is assumed.
- The backend does not require authentication for local development.
- The `suggestedFilename` is a convenience hint; the frontend may choose its own filename.
- REST calculations use ephemeral thread-isolated logging; calculator execution logs are not exposed to the frontend and do not accumulate server-side.
