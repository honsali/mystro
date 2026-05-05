# Mystro Spring Boot Conversion — Comprehensive Audit

Reviewer: Claude
Scope: full app review focused on the Spring Boot conversion + impact on existing CLI + business code under `app.basic`, `app.chart`, `app.descriptive`, `app.doctrine`, `app.input`, `app.output`, `app.runtime`, `app.web`.
Project version: 1.1.0 (per `pom.xml`).
Pre-existing audits assumed read: `basic_audit_report_claude.md`. Findings already captured there are not repeated unless the Spring conversion changes their severity or shape.

---

## 1. Executive verdict

The Spring Boot conversion is **functionally correct and well-structured at the boundary**, but it leaves the underlying CLI/server seams half-finished. The web layer cleanly added DTOs, controllers, a request mapper, isolated logging, CORS, and an exception handler. The `DescriptiveReportService` was extracted as a shared facade. The version resolution was generalized.

What's **not** clean is everything that touches process-global state: a static singleton `Logger`, two parallel object graphs (CLI vs. Spring `@Bean`), a service that bundles "generate" and "write to disk" into one class even though the web mode never writes, and a `BasicCalculator` singleton bean whose internal calculators depend on a static-looking `Logger.instance`. None of this *currently* breaks the API tests, but several are concurrency or memory-leak risks waiting to surface in any non-trivial deployment.

**Top 3 risks before going further**:
1. **Process-global `Logger` with mutable `ArrayList`** ([Logger.java:15](src/main/java/app/output/Logger.java#L15)) — thread-local `runIsolated` covers the controller's happy path, but every non-isolated `Logger.instance.info/error` call from server threads writes to a shared, unbounded, non-thread-safe list. One concurrent request that throws during `CalculationContext` construction can corrupt the list (`ConcurrentModificationException` at minimum, lost log lines or memory growth more likely).
2. **Dual wiring of the same business graph** — `App.main()` constructs `BasicCalculator/JsonReportWriter/DescriptiveReportService/DoctrineLoader` directly, while `WebConfig` constructs the same set as `@Bean`s. There is no shared assembly module. Future wiring changes will silently diverge.
3. **`MystroObjectMapper` registered as the application-wide `ObjectMapper` bean** ([WebConfig.java:50-53](src/main/java/app/web/WebConfig.java#L50-L53)) — replaces Spring's default mapper *globally*, so the 6-decimal Double rounding silently applies to any future non-API endpoint, error payload field, actuator response, etc. Probably not visible today, will absolutely surface later.

Severity-counted findings: 4 high, 8 medium, 9 low, 6 info, 5 false-positives. Details below.

---

## 2. Findings table

| # | Finding | Area | Severity | Evidence | Minimal fix |
|---|---|---|---|---|---|
| 1 | `Logger.instance` is a process-global singleton with non-thread-safe `ArrayList` | concurrency | high | [Logger.java:10-15](src/main/java/app/output/Logger.java#L10-L15) | Constrain server-mode log writes to be always isolated; or use `CopyOnWriteArrayList`/synchronized; or replace with SLF4J for non-CLI |
| 2 | Dual wiring: CLI `App.main` and `WebConfig` construct the same graph independently | architecture | high | [App.java:14-25](src/main/java/app/App.java#L14-L25) vs [WebConfig.java:60-79](src/main/java/app/web/WebConfig.java#L60-L79) | Make `App` use Spring's context too, or extract a single `assemble()` factory both call |
| 3 | `MystroObjectMapper` replaces Spring's default app-wide `ObjectMapper` | api-design | high | [WebConfig.java:50-58](src/main/java/app/web/WebConfig.java#L50-L58) | Register it only on a `MappingJackson2HttpMessageConverter` for `/api/**`, not as the global bean |
| 4 | `DescriptiveReportService` bundles "generate" + "write to disk" into one class but web only needs "generate" | architecture | high | [DescriptiveReportService.java:35-61](src/main/java/app/runtime/DescriptiveReportService.java#L35-L61) | Split into `DescriptiveReportGenerator` (pure) + `DescriptiveReportWriter` (CLI-only) |
| 5 | `BasicCalculator` registered as Spring singleton, but each `calculate(...)` `new`s ten `Calculator` impls per call | performance | medium | [BasicCalculator.java:25-34](src/main/java/app/basic/BasicCalculator.java#L25-L34) | Hold the calculators as final fields if stateless, or accept the allocation cost (negligible) and document it |
| 6 | Each request creates a fresh `SwissEph` and re-checks `Files.isDirectory("ephe")` + calls `swe_set_ephe_path` | performance | medium | [CalculationContext.java:22, 55-61](src/main/java/app/basic/CalculationContext.java#L22) | Pool/share `SwissEph` per JVM; do the dir check once at startup |
| 7 | `Subject` exposes `localBirthDateTime` separately from `resolvedUtcInstant` and both are serialized | api-design | medium | [Subject.java:48-66](src/main/java/app/input/model/Subject.java#L48-L66) | Either drop `localBirthDateTime` from the JSON response or document the redundancy |
| 8 | Empty marker `interface AstrologyReport {}` with no polymorphic use | dead-code | medium | [AstrologyReport.java](src/main/java/app/output/AstrologyReport.java) | Delete |
| 9 | Unused `DoctrineSummary` and `ReportMetadata` classes | dead-code | medium | [DoctrineSummary.java](src/main/java/app/output/DoctrineSummary.java), [ReportMetadata.java](src/main/java/app/output/ReportMetadata.java) | Delete |
| 10 | `EngineVersion.fromPomXml` reads `pom.xml` from CWD as a fallback | runtime | medium | [EngineVersion.java:70-81](src/main/java/app/runtime/EngineVersion.java#L70-L81) | Keep but verify the packaged-jar paths #1 and #2 actually resolve in repackaged Spring Boot jar; add a `@SpringBootTest` checking `engineVersion != "unknown"` |
| 11 | No `application.yml`/`.properties` — server defaults are entirely implicit | configuration | medium | no `src/main/resources/` at all | Add at least an empty `application.yml` so `mystro.cors.allowed-origins`, `server.port`, etc. are visible knobs |
| 12 | `DescriptiveController` declares `throws Exception` | api-design | medium | [DescriptiveController.java:27](src/main/java/app/web/DescriptiveController.java#L27) | Catch in a try/finally, let `GlobalExceptionHandler` handle it; don't propagate `throws Exception` |
| 13 | CLI `App.main` reaches into `Logger.instance` directly + writes a global log file | architecture | low | [App.java:23](src/main/java/app/App.java#L23) | Keep for CLI; out of scope for web review |
| 14 | `DoctrineLoader` is constructed twice (Spring bean + inside `InputLoader`) | dead-code | low | [InputLoader.java:14](src/main/java/app/input/InputLoader.java#L14) vs [WebConfig.java:71-73](src/main/java/app/web/WebConfig.java#L71-L73) | Have `InputLoader` accept a `DoctrineLoader` argument |
| 15 | `WebConfig.@Value("${mystro.cors.allowed-origins:...}")` uses field injection | code-style | low | [WebConfig.java:23-24](src/main/java/app/web/WebConfig.java#L23-L24) | Move to constructor injection or `@ConfigurationProperties` record |
| 16 | `JsonReportWriter` registered as `@Bean` but never used by web layer | dead-code | low | [WebConfig.java:65-68](src/main/java/app/web/WebConfig.java#L65-L68) | Move to a CLI-only configuration; remove from `WebConfig` |
| 17 | CORS allowed methods exclude DELETE/PUT | api-design | low | [WebConfig.java:31](src/main/java/app/web/WebConfig.java#L31) | Defensible today; revisit when API grows |
| 18 | `InputListBundle` is a mutable holder used as both input parameter and output | code-style | low | [InputListBundle.java:6-50](src/main/java/app/input/model/InputListBundle.java#L6-L50) | Replace with two records (`InputArgs` + `ResolvedInputs`); CLI-side cleanup |
| 19 | `EngineVersion.VERSION` resolved at class-load time, including the IO-heavy pom.xml fallback | runtime | low | [EngineVersion.java:23](src/main/java/app/runtime/EngineVersion.java#L23) | Lazy `Supplier<String>`; or at least don't read pom.xml unless the previous fallbacks failed (already true) |
| 20 | Test `descriptiveControllerTest` uses full `@SpringBootTest` for every test | test-cost | low | [DescriptiveControllerTest.java:19-21](src/test/java/app/web/DescriptiveControllerTest.java#L19-L21) | Use `@WebMvcTest(DescriptiveController.class)` with `@MockBean` for purely-controller assertions |
| 21 | No regression test for the actual chart numbers (snapshot of `NatalChart` JSON) | test-gap | high | `src/test/` has only Logger/Web/EngineVersion tests | Add one `@SpringBootTest` that POSTs a known birth and snapshot-asserts the report JSON |
| 22 | `MystroSpringApplication` does not register `app.App` as a `@Component`, so the CLI is invisible to Spring | architecture | info | [MystroSpringApplication.java](src/main/java/app/MystroSpringApplication.java), [App.java](src/main/java/app/App.java) | Intentional split; document |
| 23 | `pom.xml` `repackage` runs unconditionally → every build produces a Spring Boot fat jar | build | info | [pom.xml:93-99](pom.xml#L93-L99) | Acceptable; disk cost only |
| 24 | `Logger.instance.startedAt` initialized at class load, not at app start | runtime | info | [Logger.java:14](src/main/java/app/output/Logger.java#L14) | For long-running server it means "JVM start" — fine, but worth knowing |
| 25 | No security configured (no spring-security; CSRF irrelevant since no session) | security | info | absence of `spring-boot-starter-security` | Acceptable for an internal/local API; document |
| 26 | `Subject` constructor validates lat/lng twice (mapper + Subject) | code-style | info | [DescriptiveRequestMapper.java:103-111](src/main/java/app/web/DescriptiveRequestMapper.java#L103-L111) + [Subject.java:29-34](src/main/java/app/input/model/Subject.java#L29-L34) | Acceptable defense-in-depth |
| 27 | `Zodiac` enum reduced to single value `TROPICAL` | dead-code | info | [Zodiac.java](src/main/java/app/chart/data/Zodiac.java) | Single-value enum is a marker; either remove the enum entirely or keep as future-proofing slot |

Plus 5 **false positives** in §6.

---

## 3. Detailed findings

### 3.1 (#1) `Logger.instance` — process-global mutable ArrayList in a server context

**Code today**
```java
// Logger.java:10-15
public static final Logger instance = new Logger();
private static final ThreadLocal<List<LogEntry>> isolatedEntries = new ThreadLocal<>();
private final Instant startedAt = Instant.now();
private final List<LogEntry> entries = new ArrayList<>();
```

`Logger.info`/`error` either writes to the thread-local list (if `runIsolated` is active) or to the shared `entries` `ArrayList`. The web controller wraps the report generation in `runIsolated`:
```java
// DescriptiveController.java:43-45
DescriptiveAstrologyReport report = Logger.instance.runIsolated(() ->
        service.generateDescriptiveReports(resolved.bundle()).get(0)
);
```

**Why it matters**

The isolation only protects calls made *from inside the lambda*. Any `Logger.instance.info(...)` outside that lambda — e.g., from a Spring filter, a future audit log, an actuator hook, or a stray addition during refactoring — writes to the shared `ArrayList` from the request thread. `ArrayList` is not thread-safe; concurrent appends from two requests can:
- silently lose log entries
- throw `ConcurrentModificationException` from `getEntries()`'s `List.copyOf(entries)` iteration
- expand the underlying array in a race that produces an inconsistent state

Today this is masked because:
- the controller's path uses isolation,
- nobody else logs from server threads.

But the load-bearing assumption "no server-thread `Logger.instance` call ever escapes `runIsolated`" is not enforced anywhere. One forgetful future PR breaks it.

Additionally, on the CLI path, `Logger.instance.entries` grows for the entire process lifetime. On a long-running server, even *correctly isolated* requests don't grow it, but the global `entries` list is still alive holding any startup logs forever. Not a leak in practice; smell in principle.

**Real error or first-draft debt**: real architectural weakness. The `ThreadLocal` was added precisely to make the singleton survive the web conversion, but it leaves a sharp edge.

**Minimal safe fix** (pick one):
- **Option A** — wrap the entire request lifecycle in `runIsolated` via a Spring `Filter`/`HandlerInterceptor`, so any code path during a request is automatically isolated. Then any `Logger.instance.info(...)` from server threads is safe by construction. This is the smallest behavioral change.
- **Option B** — replace `app.output.Logger` calls in the calculation layer with SLF4J (`LoggerFactory.getLogger(...)`), keep `app.output.Logger` only for the CLI's run-logger output. Cleaner long-term; bigger change.
- **Option C** — make `entries` a `CopyOnWriteArrayList` or wrap appends in `synchronized`. Stops the race but doesn't address the unbounded growth.

I'd recommend Option A as the cheapest correct fix.

**Regression test**: add a concurrent test (`@SpringBootTest`) that fires N parallel requests to `/api/descriptive` with one of them deliberately invalid. Assert no `ConcurrentModificationException` and that responses are independent (no log bleed between requests).

**Over-engineering risk**: do **not** introduce a logging facade layer (LoggerFactory abstraction, event bus, etc.). The existing CLI run-log mechanism is fine; just stop letting non-isolated calls reach it from server threads.

### 3.2 (#2) Dual wiring of the same business graph

**Code today**

CLI:
```java
// App.java:14-25
InputLoader loader = new InputLoader();
JsonReportWriter reportWriter = new JsonReportWriter();
InputListBundle inputListBundle = loader.load(args);
DescriptiveReportService service = new DescriptiveReportService(new BasicCalculator(), reportWriter);
service.runDescriptive(inputListBundle);
```

Web:
```java
// WebConfig.java:60-79
@Bean public BasicCalculator basicCalculator() { return new BasicCalculator(); }
@Bean public JsonReportWriter jsonReportWriter() { return new JsonReportWriter(); }
@Bean public DoctrineLoader doctrineLoader() { return new DoctrineLoader(); }
@Bean public DescriptiveReportService descriptiveReportService(...) { ... }
```

Same four classes, two wirings. No shared assembly point. Adding a constructor argument to `DescriptiveReportService` requires editing both `App.java` and `WebConfig.java`. The IDE catches it; reviewers don't.

Plus: `InputLoader.load()` internally `new DoctrineLoader()` ([InputLoader.java:14](src/main/java/app/input/InputLoader.java#L14)) — yet another construction point for the same class.

**Why it matters**

Architecturally, you now have two entry points (`app.App.main` and `app.MystroSpringApplication.main`) that wire the same business graph two different ways. There is no single source of truth for "how is the engine assembled."

**Real error or first-draft debt**: first-draft debt from the conversion. Acceptable short-term; gets worse with each wiring change.

**Minimal safe fix**:
- **Option A (smaller)**: extract a static factory `EngineAssembly.create()` returning a record `Engine(BasicCalculator, JsonReportWriter, DoctrineLoader, DescriptiveReportService)`. Both `App.main` and `WebConfig` use it; `WebConfig` exposes the fields as `@Bean`s.
- **Option B (cleaner)**: have `App.main` start a Spring context with a CLI profile (`spring.main.web-application-type=none`), exposing the wiring as the same `@Bean`s. Then there's literally one wiring path. Costs: CLI startup time grows (Spring context init).

Either preserves CLI behavior. Option A is the smaller change.

**Test**: an architecture test asserting `BasicCalculator` is `new`'d in only one place outside `*Test.java`.

### 3.3 (#3) `MystroObjectMapper` replaces Spring's global ObjectMapper

**Code today**
```java
// WebConfig.java:50-58
@Bean
public ObjectMapper objectMapper() {
    return MystroObjectMapper.create();
}
@Bean
public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
    return new MappingJackson2HttpMessageConverter(objectMapper);
}
```

Defining an `ObjectMapper` `@Bean` overrides Spring's default. Every Jackson serialization in the *entire app context* uses this one — actuators, error responses, future endpoints, anything. Mystro's mapper has `RoundedDoubleSerializer` which truncates Doubles to 6 decimals.

**Why it matters**

Today: harmless (no other endpoints, no Double fields in error responses).
Tomorrow: someone adds an actuator endpoint, or a debug field, or an `Instant` in `LogEntry`, or anything with a Double field that *shouldn't* be rounded — and gets silently rounded. That's a footgun lurking in the wiring.

**Minimal safe fix**: scope the rounding to API responses only:
```java
@Bean
public MappingJackson2HttpMessageConverter jacksonConverter() {
    return new MappingJackson2HttpMessageConverter(MystroObjectMapper.create());
}
```
Drop the `@Bean ObjectMapper`. Spring keeps its default mapper for everything else; the converter for Mystro's API endpoints uses the rounded mapper.

If you want the converter to be applied only to `/api/**`, that requires a custom `WebMvcConfigurer.configureMessageConverters` ordering; today it would apply to all controllers, which is fine since you only have `/api/*`.

**Test**: mock a controller that returns a Double like `1.1234567890`; assert the response value is `1.123457` only when served through the API converter.

### 3.4 (#4) `DescriptiveReportService` mixes "generate" and "write to disk"

**Code today**
```java
// DescriptiveReportService.java:35-61
public List<DescriptiveAstrologyReport> generateDescriptiveReports(InputListBundle inputListBundle) { ... }
public void runDescriptive(InputListBundle inputListBundle) throws IOException {
    for (DescriptiveAstrologyReport report : generateDescriptiveReports(...)) {
        reportWriter.write(...);
        Logger.instance.info(...);
    }
}
```

The web layer only calls `generateDescriptiveReports`. The CLI calls `runDescriptive` (which writes files + logs).

But the constructor requires both:
```java
public DescriptiveReportService(BasicCalculator basicCalculator, JsonReportWriter reportWriter) { ... }
```

So in web mode, `JsonReportWriter` is held by the service but never used.

**Why it matters**
- Two responsibilities in one class.
- Web mode carries an unused dependency.
- The service can write to the global `Logger.instance` (line 58) outside any isolation, which means if a doctrine ever calls `runDescriptive` (it shouldn't, but the API is public), server-thread logs could leak.

**Minimal safe fix**: split the class:
```java
public final class DescriptiveReportGenerator {
    public DescriptiveReportGenerator(BasicCalculator basicCalculator) {...}
    public List<DescriptiveAstrologyReport> generate(InputListBundle bundle) {...}
}

public final class DescriptiveReportFileWriter {
    public DescriptiveReportFileWriter(DescriptiveReportGenerator gen, JsonReportWriter writer) {...}
    public void writeAll(InputListBundle bundle) throws IOException {...}
}
```
Web wires only the generator. CLI wires both.

**Test**: assert `DescriptiveController` doesn't depend on `JsonReportWriter` (compile-time check after the split).

### 3.5 (#5) Per-request `new` of all Calculator impls

**Code today**
```java
// BasicCalculator.java:17-37
public NatalChart calculate(CalculationContext ctx) {
    NatalChart natalChart = new NatalChart();
    (new SimpleCalculator()).calculate(natalChart, ctx);
    (new PlanetCalculator()).calculate(natalChart, ctx);
    ... // 8 more `new` calls
    return natalChart;
}
```

Each request creates 10 calculator instances. They are all stateless (verified by spot-checking: each `calculate(...)` body uses only its arguments).

**Why it matters**: pure GC pressure. Negligible at small scale, but `BasicCalculator` is a singleton bean — the *natural* shape would be to hold the 10 calculators as `final` fields. The current shape says "I look stateful but I'm actually stateless."

**Minimal safe fix**:
```java
public final class BasicCalculator {
    private final SimpleCalculator simple = new SimpleCalculator();
    private final PlanetCalculator planet = new PlanetCalculator();
    // ...
    public NatalChart calculate(CalculationContext ctx) {
        NatalChart c = new NatalChart();
        simple.calculate(c, ctx);
        planet.calculate(c, ctx);
        // ...
        return c;
    }
}
```

This makes the singleton-ness honest. Make sure each `Calculator` impl is genuinely stateless (it currently is — they hold no fields).

### 3.6 (#6) Per-request fresh `SwissEph` + repeated path-check

**Code today**
```java
// CalculationContext.java:22, 55-61
private final SwissEph swissEph = new SwissEph();
...
private void configureEphemerisPath(Subject subject) {
    if (!Files.isDirectory(Path.of(EPHEMERIS_PATH))) { ... }
    swissEph.swe_set_ephe_path(EPHEMERIS_PATH);
}
```

Each `CalculationContext` (one per request) creates a fresh SwissEph and re-validates the `ephe/` directory. SwissEph then loads ephemeris files lazily on first `swe_calc_ut`. Fresh instance = no shared file handle cache = re-open files per request.

**Why it matters**
- File I/O per request for the directory check.
- SwissEph internal caches (the `.se1` file binary data) are not reused between requests. Each chart re-reads the same files.

**Minimal safe fix**:
- Validate `ephe/` once at app start (in `WebConfig` or a `@PostConstruct` hook).
- Either make `SwissEph` a Spring singleton bean shared across requests (verify thread-safety of `SwissEph` first — most Swiss Eph Java ports are *not* thread-safe; see below), OR pool a small number of instances and lend one per request.

**Important caveat**: SwissEph instances commonly hold mutable internal state during `swe_calc_ut` (working buffers, file pointers). If `SwissEph` is not thread-safe, sharing one across concurrent requests is a correctness bug, not a performance gain. **Verify before sharing**. If unsafe, a `ThreadLocal<SwissEph>` per request thread is a safe middle ground.

**Test**: a benchmark `@SpringBootTest` that fires 10 sequential and 10 concurrent requests, asserts identical JSON output, and prints the request times. Don't optimize before measuring.

### 3.7 (#7) `Subject` exposes both `localBirthDateTime` and `resolvedUtcInstant`

**Code today**
```java
// Subject.java
public OffsetDateTime getLocalBirthDateTime() { ... }
public Instant getResolvedUtcInstant() { ... }
```

Both serialize into the response JSON. They represent the same moment in time, just two different views.

**Why it matters**
- Bigger payload than necessary.
- API consumers may pick one and ignore the other; if they pick the wrong one, future timezone bugs.
- The two views are guaranteed-consistent only if the `Subject` was constructed via the single-arg ctor; the two-arg ctor lets them disagree.

**Minimal safe fix**: pick one for the API contract. Recommend `resolvedUtcInstant` (unambiguous). Add `@JsonIgnore` to the other getter or split `Subject` into a domain object and a JSON DTO.

### 3.8 (#8 & #9) Dead types: `AstrologyReport`, `DoctrineSummary`, `ReportMetadata`

```java
// AstrologyReport.java
public interface AstrologyReport {}
```

`DescriptiveAstrologyReport implements AstrologyReport` but no code depends on the interface. `List<AstrologyReport>`, `AstrologyReport variable`, `instanceof AstrologyReport` — none exist. Pure marker noise.

Grep confirms `DoctrineSummary` and `ReportMetadata` are referenced by exactly one file each (their own definition). Pure dead code.

**Minimal safe fix**: delete all three. If `AstrologyReport` is intended as a future polymorphism slot for predictive reports, leave a comment explaining that — otherwise it just clutters the package.

### 3.9 (#10) `EngineVersion.fromPomXml` reads `pom.xml` from CWD

**Code today** ([EngineVersion.java:70-81](src/main/java/app/runtime/EngineVersion.java#L70-L81))
```java
private static String fromPomXml() {
    try {
        String pom = Files.readString(Path.of("pom.xml"));
        ...
```

The *intended* resolution is from the package implementation version (works in packaged jars) → maven-properties → pom.xml fallback → "unknown". But there's no test verifying that `fromPackageImplementation` actually returns non-null in the repackaged Spring Boot jar.

**Real concern**: Spring Boot's `repackage` goal nests the application jar inside `BOOT-INF/classes/`, so `EngineVersion.class.getPackage().getImplementationVersion()` depends on whether the manifest is properly populated. Specifically, the manifest needs `Implementation-Version`, which is populated by the `maven-jar-plugin` only if explicitly configured (it isn't here).

The `pom.properties` path (`/META-INF/maven/mystro/mystro/pom.properties`) is added by `maven-jar-plugin`'s default behavior and *does* reach the classpath in the repackaged jar.

**Practical risk**: if neither path #1 nor #2 resolves in the deployed jar, the `pom.xml` fallback fails (CWD won't have `pom.xml`), and the report says `"engineVersion": "unknown"`. The current `EngineVersionTest` only verifies dev/CWD path.

**Minimal safe fix**:
1. Add a `@SpringBootTest`-loaded test that builds the repackaged jar and asserts `EngineVersion.get()` is not "unknown" — or equivalently, an assertion in `MystroSpringApplication.main` startup that fails-fast if the version is "unknown."
2. Configure `maven-jar-plugin` to add `Implementation-Version: ${project.version}` to the manifest, eliminating the dependency on the secondary fallback.

**Over-engineering risk**: don't pull in spring-boot-actuator's `/info` endpoint just for this — the existing field on the report is enough.

### 3.10 (#11) No `application.yml` / `application.properties`

There is no `src/main/resources/` directory at all. Every Spring Boot setting is implicit:
- `server.port=8080` (default)
- `mystro.cors.allowed-origins=http://localhost:5173,http://localhost:3000` (hardcoded fallback in WebConfig)
- All Spring auto-config defaults

**Why it matters**: production deployment requires overriding via `--server.port=...`, env vars, or a mounted config file. None of this is discoverable without reading `WebConfig.java`. Future ops engineer has nothing to grep for.

**Minimal safe fix**: add `src/main/resources/application.yml` with the *current default values explicitly written*:
```yaml
server:
  port: 8080
mystro:
  cors:
    allowed-origins: "http://localhost:5173,http://localhost:3000"
```
This makes the configuration surface visible without changing behavior.

### 3.11 (#12) Controller declares `throws Exception`

```java
// DescriptiveController.java:27
@PostMapping("/descriptive")
public ResponseEntity<?> descriptive(@RequestBody(required = false) DescriptiveRequest request) throws Exception {
```

The `throws Exception` exists because `Logger.instance.runIsolated(...)` rethrows `Exception` (from `Callable<T>.call()`). The exception is then caught by `GlobalExceptionHandler.handleGeneric` → 500.

**Why it matters**: `throws Exception` on a controller signature is a code smell. It signals the controller doesn't know what can fail. It also forces callers (tests) to declare the same.

**Minimal safe fix**: change `runIsolated` to take a `Supplier<T>` or rethrow as `RuntimeException`:
```java
public <T> T runIsolated(Supplier<T> supplier) { ... }
```
Then the controller signature becomes clean.

### 3.12 (#13–#14) Minor wiring duplication

`DoctrineLoader` instantiated 3 places: `WebConfig`, `InputLoader.load` ([InputLoader.java:14](src/main/java/app/input/InputLoader.java#L14)), and the Spring bean. Trivial fix: pass the loader into `InputLoader`.

### 3.13 (#15) `WebConfig` field-injected `@Value`

```java
@Value("${mystro.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
private String allowedOrigins;
```

Field injection makes the class harder to test (the `parseOrigins` test works around this by being static). Constructor injection would be cleaner. Or extract to `@ConfigurationProperties`:
```java
@ConfigurationProperties(prefix = "mystro.cors")
public record CorsProperties(String allowedOrigins) {}
```

Defer until there are more knobs.

### 3.14 (#20) `@SpringBootTest` for every controller test

`DescriptiveControllerTest` uses `@SpringBootTest` + `@AutoConfigureMockMvc`, which loads the full Spring context for each test class. Acceptable for ~15 tests today. As tests grow, prefer `@WebMvcTest(DescriptiveController.class)` + `@MockBean DescriptiveReportService` for tests that don't need the full graph.

### 3.15 (#21) **No regression test for chart numbers**

Current tests:
- `LoggerTest` — unit test of the logger
- `WebConfigTest` — unit test of `parseOrigins`
- `EngineVersionTest` — version resolution
- `GlobalExceptionHandlerTest` — exception handling
- `DescriptiveControllerTest` — endpoint contract, top-level fields, validation, CORS

**No test asserts on actual planetary positions, sect, dignities, syzygy, or any astrology number.** A change in `BasicCalculator` ordering, `swe_set_ephe_path` resolution, or `RoundedDoubleSerializer` precision would silently change every chart and no test would fire.

This is the same gap I flagged in `basic_audit_report_claude.md`. The Spring Boot conversion makes it easier to add: write *one* `@SpringBootTest` that POSTs `ilia` to `/api/descriptive` and snapshot-asserts the full JSON response. From then on, every calculation change either preserves the snapshot or is an explicit re-baseline.

This is the **single highest-leverage test to add**.

---

## 4. Web layer specifics — what was done well

To balance: the web layer additions are *mostly* well-thought-out.

- **DTO separation**: `DescriptiveRequest`/`DescriptiveResponse`/`ErrorResponse` are pure DTOs, not domain leakage. ✓
- **Mapper isolation**: `DescriptiveRequestMapper` translates JSON → domain with explicit, testable validation. ✓
- **Exception handling**: `GlobalExceptionHandler` covers malformed body, validation, generic exception with explicit `Cache-Control: no-store` and consistent `ErrorResponse`. ✓
- **`Cache-Control: no-store` on every response** — appropriate for a calculation API where input variability could otherwise be cached incorrectly upstream. ✓
- **Origin allowlist with safe defaults** — `parseOrigins` falls back to localhost only. ✓
- **CORS preflight tested for both allowed origins and a denied origin** — explicit negative test. ✓
- **`@RestControllerAdvice`** catches `HttpMessageNotReadableException` so a malformed body returns a clean 400 instead of a generic 500. ✓
- **Engine version resolution** has a 3-stage fallback. The strategy is correct; the test coverage is just incomplete (#10).
- **Per-request log isolation** via `Logger.runIsolated(...)` — the *idea* is right; the execution leaves room for non-isolated calls (#1).

---

## 5. Spring Boot specifics that are missing or weak

- No `application.yml` (#11)
- No actuator (`/health`, `/info`) — fine for now, but a deployed service will want at least `/health`
- No request logging filter — every Spring Boot starter web includes Tomcat access logs as opt-in via `server.tomcat.accesslog.enabled=true`; not configured
- No graceful shutdown timeout configured (`server.shutdown=graceful`)
- No request size limit configured (`spring.servlet.multipart.max-request-size`)
- No structured logging configuration (defaults to logback console)
- No build-info file (`spring-boot-maven-plugin <executions><goals><goal>build-info</goal></goals></executions>`) — would give actuator `/info` automatic version info if actuator is added later
- The `mainClass` for `exec-maven-plugin` is `app.App` and for `spring-boot-maven-plugin` is `app.MystroSpringApplication`. These two coexist but it's not obvious which command runs which mode. Document in README:
  - `mvn spring-boot:run` → web mode
  - `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"` → CLI mode
  - Packaged jar (`java -jar target/mystro-1.1.0.jar`) → web mode

---

## 6. False positives / do not "fix"

| Item | Why not |
|---|---|
| `MystroSpringApplication.main(args)` doesn't pass args to the CLI | Intentional split between CLI (`app.App`) and web (`MystroSpringApplication`); they are separate entry points by design |
| `BasicCalculator` registered as singleton bean despite per-call `Calculator` allocation | Currently safe (calculators are stateless); finding #5 is an optimization, not a correctness issue |
| `Cache-Control: no-store` on every `/api/descriptive` response, including errors | Correct — response varies with input; should never be cached |
| `Subject` constructor double-validates lat/lng | Defense-in-depth between API mapper and domain object — fine |
| `DoctrineLoader` registered manually rather than via Spring `@Component` discovery | Per `NEW_ARCHITECTURE_SPEC.md`: doctrines are knowledge modules, not plugins. Manual registration is intentional |
| No spring-boot-starter-security | Acceptable for a local/internal calculation API; documenting the deployment assumption is enough |

---

## 7. CLI vs. server consistency check

| Concern | CLI | Web | Same? |
|---|---|---|---|
| `BasicCalculator` instantiation | `new BasicCalculator()` in `App.main` | `@Bean` in `WebConfig` | Different instance, same class ✓ |
| `JsonReportWriter` instantiation | `new JsonReportWriter()` in `App.main` | `@Bean` in `WebConfig` (unused by web) | Different instance, same class ⚠ web bean wasted |
| `DoctrineLoader` instantiation | `new DoctrineLoader()` inside `InputLoader.load()` | `@Bean` in `WebConfig` | Three different instances total ⚠ |
| `ObjectMapper` configuration | `MystroObjectMapper.create()` in `JsonReportWriter` | `MystroObjectMapper.create()` in `WebConfig` | Same configuration, two instances ✓ but drift risk |
| Logging | `Logger.instance.info/error` direct (non-isolated) | Wrapped in `runIsolated` per request | Different lifecycle ✓ but #1 risk |
| Subject construction | `SubjectListParser` parses `input/subject-list.json` | `DescriptiveRequestMapper` parses `DescriptiveRequest` | Different sources, same `Subject` validation ✓ |
| Output | Writes to disk via `JsonReportWriter` | Returns in HTTP response (`DescriptiveResponse`) | Different shape (web wraps with `suggestedFilename`) ✓ deliberate |
| Engine version | `EngineVersion.get()` at report construction | Same | ✓ identical |

The two paths produce **the same `NatalChart`** for the same input, but the *report wrapper* differs:
- CLI: `DescriptiveAstrologyReport` written directly as JSON → `{engineVersion, subject, doctrine, natalChart}`
- Web: `DescriptiveResponse{report: <DescriptiveAstrologyReport>, suggestedFilename: <string>}`

This is asserted by `descriptiveControllerTest.descriptiveReturnsSingleReportWithExpectedTopLevelFields` ([line 106-123](src/test/java/app/web/DescriptiveControllerTest.java#L106-L123)). The web shape is correct and intentional.

---

## 8. Prioritized refactor/test plan (max 5)

### Action 1 — Add the chart-number regression test (finding #21)
**Effort**: 2 hours. **Risk**: zero (pure addition).
Write a `@SpringBootTest` that POSTs the `ilia` fixture to `/api/descriptive` and snapshot-asserts the full JSON. After this lands, every other refactor below has a safety net.

### Action 2 — Wrap the entire request lifecycle in `Logger.runIsolated` via a Spring `Filter` (finding #1)
**Effort**: 2 hours. **Risk**: low.
Removes the load-bearing assumption that "no server-thread `Logger.instance` call escapes runIsolated." Stops the `ConcurrentModificationException` risk.
While there: change `runIsolated` to take a `Supplier<T>` (no checked exceptions) so the controller can drop `throws Exception` (finding #12).

### Action 3 — Scope `MystroObjectMapper` to the API converter only (finding #3)
**Effort**: 30 minutes. **Risk**: low — `descriptiveControllerTest.descriptiveRoundsDoublesToSixDecimals` will still pass.
Drop `@Bean ObjectMapper`; keep only the `MappingJackson2HttpMessageConverter`. Eliminates the silent-rounding-of-future-Doubles footgun.

### Action 4 — Split `DescriptiveReportService` into `DescriptiveReportGenerator` + `DescriptiveReportFileWriter` (finding #4)
**Effort**: 1 hour. **Risk**: low — Action 1's snapshot test catches any output regression. Both CLI and web get cleaner dependencies. Web no longer drags `JsonReportWriter`.

### Action 5 — Delete dead code (`AstrologyReport`, `DoctrineSummary`, `ReportMetadata`) and add `application.yml` (findings #8, #9, #11)
**Effort**: 30 minutes. **Risk**: zero (deletes + new file with current defaults).
Cleans the `app.output` package; gives ops a discoverable config surface.

**Findings #2 (dual wiring), #5–#7, #13–#15, #17–#20** are sub-hour cleanups bundled as a "Spring conversion polish" PR after Actions 1–5 land. None are blockers.

---

## 9. Reviewer self-check

- [x] Reviewed every changed/added file in `app.web`, `app.runtime`, and tests.
- [x] Cross-checked CLI path still works (`App.main` independent of Spring).
- [x] Verified pre-existing `app.basic` findings (sidereal already removed, JD source unified, `applyPlanetSects` now a Calculator). Several earlier findings are resolved.
- [x] Did not propose security framework, plugin discovery, or interpretation in `app.basic`.
- [x] Every finding has file:line evidence and a minimal fix.
- [x] Distinguished spec-intentional behavior (false positives) from real risks.
- [x] Plan is prioritized and bounded to 5 actions, each preserving the JSON shape and the doctrine-owned descriptive contract.
