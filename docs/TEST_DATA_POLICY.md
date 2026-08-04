# Test-data privacy policy

Mystro's committed tests and documentation must use synthetic, non-personal natal data only.

## Canonical synthetic fixture

Calculation tests share `app.testing.SyntheticTestData`, which uses the conventional J2000
reference date and Greenwich. This fixture does not describe a Mystro user. Tests that need a
different boundary condition may define another fixture only when its synthetic purpose is clear
from the identifier and surrounding test.

## Never commit

- real names or aliases connected to natal data;
- real birth dates, birth times, or birth coordinates;
- `native-list.json` or generated files under `output/`;
- session transcripts, assistant memory, private notes, or copied user prompts;
- snapshots generated from a real natal chart, even if the name is removed.

Before publishing, search the complete working tree for the private alias and each identifying
field separately. Renaming a subject is not anonymization because a natal chart can fingerprint
the underlying birth data.
