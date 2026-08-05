# Mystro

Mystro is an offline Java 25 calculation engine for traditional astrology. It reads one local
subject by alias, produces a Valens-led natal reading bundle, and writes calculated evidence for
use by humans or an external AI assistant.

> Documentation axis: installation and first successful run only.

## Requirements

- JDK 25;
- Maven 3.9 or newer;
- `org.swisseph:swisseph-java-ffm:0.3.0` from GitHub Packages;
- Swiss Ephemeris C 2.10.03 native library;
- Swiss Ephemeris data files.

From the repository root, provide the platform library and ephemeris data locally:

```text
dll/swedll64.dll     # Windows
dll/libswe.so        # Linux
dll/libswe.dylib     # macOS
ephe/                # Swiss Ephemeris data
```

`dll/`, `ephe/`, and `native-list.json` are intentionally ignored by Git. The native library may
instead be selected with `-Dswisseph.library.path=<absolute-path>` or the
`SWISSEPH_LIBRARY` environment variable.

GitHub Packages may require Maven credentials for repository id `github` in a fresh environment.

## Build and verify

```bash
mvn test
mvn package -DskipTests
```

## Local input

Create an ignored `native-list.json` in the repository root. This example is synthetic:

```json
[
  {
    "name": "demo",
    "birth_date": "01/01/2000",
    "birth_time": "12:00",
    "latitude": 51.4769,
    "longitude": 0.0,
    "elevation_meters": 0.0,
    "utc_offset": "+00:00",
    "inquiry_date": "15/01/2025"
  }
]
```

`birth_date` and `inquiry_date` use `dd/MM/yyyy`. `birth_time` accepts `HH:mm` or `HH:mm:ss`.
`inquiry_date` and `elevation_meters` are optional.

## Run the natal calculation

POSIX shell:

```bash
MAVEN_OPTS="--enable-native-access=ALL-UNNAMED" mvn -q compile exec:java -Dexec.args="demo"
```

PowerShell:

```powershell
$env:MAVEN_OPTS="--enable-native-access=ALL-UNNAMED"
mvn -q compile exec:java '-Dexec.args=demo'
```

The result is written to `output/demo/reading_output.json`.

## Documentation map

Each document owns one subject:

- [Project vision](docs/PROJECT_VISION.md) — product purpose and boundaries;
- [Reading tasks](docs/READING_TASKS_SPEC.md) — traditional-astrology task taxonomy;
- [Technical specification](docs/SPECIFICATION.md) — current architecture and contracts;
- [Local life-arc workflow](docs/LOCAL_MODULE2_WORKFLOW.md) — macro and zoom report generation;
- [AI analysis questions](docs/QUESTIONS_ANALYSE_IA_FR.md) — downstream interpretation prompts;
- [Test-data policy](docs/TEST_DATA_POLICY.md) — privacy rules for committed fixtures;
- [Publishing checklist](docs/PUBLISHING_CHECKLIST.md) — repository publication checks.
