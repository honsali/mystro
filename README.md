# Mystro

Mystro is an offline Java command-line calculation engine for traditional astrology. The current
application loads one alias from a local `native-list.json`, calculates a Valens-led natal
description, and writes `output/<alias>/reading_output.json`.

## Requirements

- Java 25
- Maven 3.9 or newer
- `org.swisseph:swisseph-java-ffm:0.2.0`
- Swiss Ephemeris C 2.10.03 shared library:
  - `dll/swedll64.dll` on Windows
  - `dll/libswe.so` on Linux
  - `dll/libswe.dylib` on macOS
- Swiss Ephemeris runtime data in `ephe/`

The legacy-compatible `libs/` directory is also searched. The native library can instead be configured with
`-Dswisseph.library.path=<absolute-path>` or the `SWISSEPH_LIBRARY` environment variable. Native
access must be enabled with `--enable-native-access=ALL-UNNAMED`. Mystro's shaded executable JAR
declares this access in its manifest; Maven-based launches still need `MAVEN_OPTS`. The native
binary and the `ephe/` data are not supplied by the Java binding.

The Java dependency is resolved from GitHub Packages. Version `0.2.0` must be published there
before building Mystro in a fresh Maven environment. That environment must authenticate the
repository id `github` in its Maven settings until the artifact is also published to a repository
that supports anonymous Maven downloads.

## Build and verify

```bash
mvn test
mvn package -DskipTests
```

## Local input

`native-list.json` is intentionally ignored. This synthetic example uses the J2000 reference date
and Greenwich; replace it locally with the subject you want to calculate, but never commit real
natal data.

```json
[
  {
    "name": "demo",
    "birth_date": "01/01/2000",
    "birth_time": "12:00",
    "latitude": 51.4769,
    "longitude": 0.0,
    "utc_offset": "+00:00",
    "inquiry_date": "15/01/2025"
  }
]
```

Run the natal JSON calculation:

```bash
MAVEN_OPTS="--enable-native-access=ALL-UNNAMED" mvn -q compile exec:java -Dexec.args="demo"
```

PowerShell equivalent:

```powershell
$env:MAVEN_OPTS="--enable-native-access=ALL-UNNAMED"
mvn -q compile exec:java '-Dexec.args=demo'
```

Generate the gated local research packs:

```bash
./run demo
./run demo 15/06/2024
```

The Windows equivalents are `run.bat demo` and `run.bat demo 15/06/2024`.

## Documentation

- `docs/PROJECT_VISION.md`
- `docs/READING_TASKS_SPEC.md`
- `docs/SPECIFICATION.md`
- `docs/LOCAL_MODULE2_WORKFLOW.md`
- `docs/TEST_DATA_POLICY.md`
- `docs/PUBLISHING_CHECKLIST.md`
