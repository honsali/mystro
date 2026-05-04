Part 1 — Sync with code
Hard mismatches (will mislead an agent)
1. Wrong input filename (5 docs) — Docs say input/native-list.json, actual file is input/subject-list.json (confirmed via SubjectListParser.java:25 and the filesystem).

AGENTS.md:60
NEW_ARCHITECTURE_SPEC.md:96, 100
README.md:33, 53
SESSION_MEMORY.md:49
.pi/skills/astro-chart-workflow/SKILL.md:30
2. "Reference validation" pipeline stage doesn't exist in code — Listed as the final pipeline step in 5 places, but App.java has no such stage. Either remove from docs or note it as aspirational.

README.md:17
NEW_ARCHITECTURE_SPEC.md:33
AGENTS.md:52
SESSION_MEMORY.md:19
.pi/skills/astro-chart-workflow/SKILL.md:23
3. Engine version drift — App.java:17 hardcodes ENGINE_VERSION = "0.1.0". pom.xml is at 0.14.0. SESSION_MEMORY.md:84 says "Current Maven version for the next development cycle is 0.12.0" — stale by two cycles. The hardcoded "0.1.0" in App.java is the value that ends up in every report's top-level engineVersion — that's almost certainly a bug in App.java, not the docs.

4. Sidereal positioning in docs is now incorrect for this project's scope — Multiple docs say sidereal is "intentionally absent until an explicit ayanamsa model and doctrine requirement are introduced," implying it's a future feature. Per our conversation, the entire Valens-to-Lilly canon is exclusively tropical; sidereal is out of scope, not "deferred." Worth rewording, and removing the dead Zodiac.SIDEREAL enum value + its planetFlags()/houseFlags() branches in the code so the docs stay honest.

AGENTS.md:65
NEW_ARCHITECTURE_SPEC.md:156
SESSION_MEMORY.md:55
5. Doctrine package path — NEW_ARCHITECTURE_SPEC.md:386-388 lists descriptive packages but never mentions that doctrine impls live under app.doctrine.impl.<name> (not app.doctrine.<name>). Minor, but a fresh agent will guess wrong.

Verified accurate (cross-checked against code)
Pipeline ordering of basic calculators ✓
Doctrine extends CalculationDefinition contract ✓
Top-level report keys (engineVersion, subject, doctrine, calculationSetting, natalChart) ✓
natalChart.points keyed by point name ✓
pairwiseRelations shape including contraParallelSeparation ✓
Sect baseline altitude >= 0.0 ✓
JD derived from subject.getResolvedUtcInstant() ✓
--subjects/--doctrines CLI ✓
Doctrine list (valens, ptolemy, dorotheus) ✓
Valens vs Ptolemy vs Dorotheus coverage descriptions ✓
app.old removed (no longer present on disk; AGENTS.md line 58 still mentions it as "migration material" — stale, can drop)
Part 2 — Multi-session agent completeness
What a fresh agent cannot answer from current docs:

A. How to verify a calculation change didn't regress — There's no src/test/, no reference outputs, no checklist of "for subject X, the Sun should land at Y." An agent making a calculation change has only mvn compile (does it build?) and mvn exec:java ... (did it run?), but no signal on whether the numbers are still right. This is the biggest gap for a precision-targeted engine.

B. Java version requirement — pom.xml requires Java 17; no doc mentions it. An agent on a different JDK gets confusing errors.

C. Swiss Ephemeris data files — The ephe/ directory contains the SE data files required for swe_calc_ut to work. Not mentioned in any doc. An agent might "clean up" this directory thinking it's stale.

D. input/settings.properties is silently optional — SettingLoader.java reads it for calculation.precision. Not documented anywhere. A fresh agent won't know this knob exists.

E. How to add a new doctrine — The most common future task. Pattern exists (3 examples) but no walkthrough: register in DoctrineLoader, implement Doctrine, choose calculation choices, write descriptive calculator, pour into NatalChart via apply* methods.

F. Recent precision/architecture decisions are not in durable docs — From this conversation:

Geocentric positions are intentional (matches pre-modern practice)
Lunar parallax is not corrected (deliberate)
Apparent positions (light-time, aberration, gravitational deflection) intentional
Polar Placidus = fail-fast, no fallback
Moon at exactly 180° elongation = waxing (convention)
A future agent will re-discover each of these and may "fix" them. Worth a single "Intentional calculation conventions" section.

G. Doc redundancy / drift risk — AGENTS.md, NEW_ARCHITECTURE_SPEC.md, SESSION_MEMORY.md, and README.md substantially overlap. The current /version workflow (.pi/prompts/version.md) explicitly tells agents to update all three of AGENTS/SESSION_MEMORY/SPEC, but the duplication itself invites drift. ASTRO_RULES.md is near-empty and just defers to the spec — candidate for deletion.

Suggested next steps (in priority order)
Fix the input filename in all 5 docs — single highest-impact correction.
Fix the engineVersion — either auto-load from pom.xml or update the hardcoded constant; then drop the stale "next cycle is 0.12.0" line from SESSION_MEMORY.
Drop "Reference validation" from the pipeline diagram in all 5 docs (or implement it).
Reword sidereal from "deferred" to "out of scope; this engine targets the Valens-to-Lilly tropical tradition exclusively," and delete Zodiac.SIDEREAL and the related code branches.
Add a short "Intentional calculation conventions" section to NEW_ARCHITECTURE_SPEC.md capturing the geocentric/parallax/apparent-position/polar/moon-180° decisions so future agents don't try to "fix" them.
Add an "Environment" section somewhere (probably README): Java 17, ephe/ files required, input/settings.properties optional knob.
Decide on consolidation: either delete ASTRO_RULES.md and the skill that references it, or give it real content. As-is it's a redirect that costs an extra hop.
If you want, I can apply the docs-only fixes (1, 3, 4, 5, 6, 7) — they're mechanical. The engineVersion fix (#2) is a code change.