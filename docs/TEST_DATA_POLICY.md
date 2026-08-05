# Test-Data Privacy Policy

> Documentation axis: which data may be committed as tests, oracles, examples, and snapshots.

## Allowed fixtures

Committed calculation tests use synthetic, non-personal data. The canonical reusable fixture is
`app.testing.SyntheticTestData`, based on the conventional J2000 instant and Greenwich.

A boundary test may define a different date or location when its synthetic purpose is explicit in
the test identifier. Fixed Swiss Ephemeris oracle rows are allowed when they are documented as
synthetic regression cases.

## Local-only data

The following data stays local and Git-ignored:

- a real name or alias connected to natal data;
- a real birth date, birth time, UTC offset, or birth coordinates;
- `native-list.json`;
- generated files under `output/`;
- snapshots derived from a real natal chart, even after removing the name;
- assistant transcripts, session memory, private prompts, or local notes.

The combined birth instant and coordinates can identify the underlying chart even when its subject
name changes.

## Review rule

When a test needs data outside the canonical fixture, reviewers must be able to understand from the
code alone why the values are synthetic and which boundary they exercise.
