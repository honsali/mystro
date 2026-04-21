VALIDATION REPORT — 2026-04-21 — mvn exec run at 2026-04-21T15:41

Charts validated: ilia (nocturnal, Lille 1975-07-14), marwa (nocturnal, Paris 2001-12-11), reda (diurnal, Rabat 2017-10-04)

=== SUMMARY ===
Sections checked: 11 per chart (birth, planetaryHour, syzygy, lots, planets, houses, mainAspects, otherAspects, dodecatemoria, novenaria, antiscia, contraAntiscia)
MATCH charts: 1 / 3 (ilia)
DIFF charts: 2 / 3 (marwa, reda)
MATCH is algorithmically misleading in at least one case (see ALGORITHMIC CONCERNS).
DIFFs outside tolerance: 5 distinct root causes
Algorithmic concerns (independent of comparison): 3

=== DIFFS OUTSIDE TOLERANCE ===

1.
   Chart: reda
   Section: lots (5 of 7)
   Fields: Eros, Victory, Necessity, Nemesis — sign/signLon/house/ruler mirrored through the Ascendant (offsets 2·Asc mod 360). Single root cause.
   Mystro values: Eros=Sag 0.67°; Victory=Libra 19.77°; Necessity=Aries 23.62°; Nemesis=Cancer 7.87°
   Astro-Seek values: Eros=Pisces 2.40°; Victory=Aries 13.30°; Necessity=Libra 9.48°; Nemesis=Cancer 25.23°
   Delta: ≥ 180° of arc per lot (signs reversed)
   Likely cause: The five derived Hermetic Lots (Eros, Victory, Necessity, Courage, Nemesis) are computed with FIXED arc directions that ignore sect. Mystro always builds these as (Asc + p1 − p2) where the (p1, p2) pair is hard-coded to the NOCTURNAL pattern for Eros/Victory/Necessity/Nemesis and to the DIURNAL pattern for Courage. For reda (a diurnal chart) this produces the nocturnal formula for 4 of 5 lots. Mystro's Fortune and Spirit DO invert by sect; the derived lots do not.
   Source location: src/main/java/app/mystro/processor/impl/HermeticLotsProcessor.java:21-25 (five buildLot() calls with hard-wired argument order; compare with the sect-conditional block at lines 16-17 for Fortune and Spirit).
   Severity: BLOCKING — every derived lot is wrong for every diurnal chart, and Courage is wrong for every nocturnal chart.

2.
   Chart: marwa
   Section: planets.Syzygy (and cascading dodecatemoria / novenaria / antiscia / contraAntiscia of Syzygy)
   Field: Syzygy.sign
   Mystro value: Sagittarius 8.72° (absLon 248.72°)
   Astro-Seek value: Gemini 8.72° (absLon 68.72°)
   Delta: 180°
   Likely cause: PlanetPositionsProcessor.addSyzygyPosition unconditionally computes the **Sun's** longitude at the prenatal lunation datetime, regardless of sect or New/Full phase. Marwa's prenatal syzygy is a Full Moon and her birth is nocturnal, so under both standard Hellenistic conventions (Brennan's "sect light at syzygy" and Valens's "luminary above the horizon at syzygy") the correct choice is the Moon at that moment — which is exactly 180° from the Sun and produces Gemini 8.72°. All four derived-chart Syzygy fields (dodec, novenaria, antiscion, contra-antiscion) cascade self-consistently from Mystro's wrong Syzygy longitude; once the Syzygy fix is applied, all four derivatives should move into tolerance with Astro-Seek.
   Source location: src/main/java/app/mystro/processor/impl/PlanetPositionsProcessor.java:76 (`swe_calc_ut(jd, SweConst.SE_SUN, ...)` hard-coded).
   Severity: BLOCKING for marwa and for any Full-Moon/nocturnal (or New-Moon/diurnal with Sun-below-horizon) combination; cascades into 4 downstream sections.

=== DIFFS WITHIN TOLERANCE or NON-BUGS ===

3. Chart marwa — mainAspects.size 3 vs 5
   Mystro's uniform orb table (Conj/Opp 8.0°, Square/Trine 6.0°, Sextile 5.5°) rejects Sun-Venus conj (orb 8.03°) and Sun-Saturn opp (orb 9.15°). Astro-Seek admits these (treats luminary orbs more generously). The orb VALUES themselves match Astro-Seek within 0.01°. Index-shifted differences in mainAspects[1]/[2]/[3] are artifacts of the list-length mismatch, not per-pair computation errors.
   Source location: src/main/java/app/mystro/processor/impl/AspectsProcessor.java:17-22 (DEGREE_ASPECTS table).
   Severity: MINOR / policy — not a calculation bug; if parity with Astro-Seek's luminary-aspect set is desired, widen Conj/Opp orb to ≥ 9.5° or introduce per-planet orb rules.

4. Chart marwa — planets.Uranus.retrograde true (AS) vs false (Mystro, speed=+0.03°/d); planets.Pluto.retrograde true (AS) vs false (Mystro, speed=+0.04°/d).
   Birth 2001-12-11 22:29 UT is within hours of Uranus's direct station (ephemerally ~Dec 11, 2001), and Pluto had already stationed direct in early December 2001. Mystro's ephemeris-derived positive speeds are astronomically correct; Astro-Seek's HTML displays a retrograde marker that persists across the station, which the AstroSeekParser then ingests as `retrograde=true`. The disagreement is a parsing/display artifact on the reference side, not a Mystro error — but flag it because the validation set relies on that HTML as the oracle.
   Severity: MINOR (oracle-side artifact, not a Mystro bug). Worth a guard in the comparator to downgrade retrograde disagreements within ±0.1°/d of station.

5. Chart marwa — planetaryHour.next 75.86 vs 75.0; last -2.48 vs -2.0.
   Mystro returns fractional minutes (double). The Astro-Seek HTML exposes integer minutes only, and AstroSeekParser reads them as integers. Delta 0.86 min ≈ 52 s, entirely within the expected float-vs-truncated precision gap. Not a bug.
   Severity: noise.

=== ALGORITHMIC CONCERNS (MATCH may hide shared error or formula diverges from tradition) ===

A. Lot **Courage** is wrong for every NOCTURNAL chart but appears MATCH vs Astro-Seek.
   Finding: For both nocturnal charts (marwa, ilia) Mystro produces Courage via Asc + Mars − Fortune, which is the DIURNAL Paulus/Brennan formula. Astro-Seek's parser emits the same value, so report.json marks the field MATCH. Under the sect convention codified in validation-agent.md lines 119-124 and in Brennan (Hellenistic Astrology, Table 15.1), the nocturnal Courage formula is Asc + Fortune − Mars. For marwa, the correct Courage longitude is 20.87° Aries; Mystro (and Astro-Seek) report 25.73°/25.72° Capricorn — an identical error in two implementations.
   Reference source: Paulus Alexandrinus / Brennan, "Hellenistic Astrology" (2017), Ch. 15, lot table; also the explicit formulas in validation-agent.md §Hermetic Lots.
   Recommendation: Treat the shared-MATCH on Courage as a false positive. The fix is the same one BLOCKING #1 requires: make all five derived lots sect-conditional in HermeticLotsProcessor.

B. Chiron is NOT computed from Swiss Ephemeris — it is read back from the saved Astro-Seek HTML.
   Finding: ChartProcessor.java shows no SE_CHIRON calc_ut call; addChironFallback (lines 116-137) is the ONLY Chiron source, and it parses `Chiron,<sign>,<deg>&deg;<min>` from `<name>.html`. Across all three charts Mystro's Chiron agrees with Astro-Seek to within 0.01° because it IS Astro-Seek's value. This is exactly the masking scenario flagged in validation-agent.md §Planet positions. Chiron's MATCH status on all three charts carries no validation weight whatsoever.
   Reference source: validation-agent.md §Planet positions, "Chiron fallback" paragraph.
   Recommendation: Add `addPlanet(points, "Chiron", SweConst.SE_CHIRON, jd)` to the main planet sequence in ChartProcessor.populate() and retain the HTML fallback only for dates outside ephemeris range. Re-run the three charts afterward to obtain a genuine Chiron validation signal.

C. Uniform orb policy may diverge from the project's declared orb tradition.
   Finding: AspectsProcessor uses fixed orbs of 8.0°/6.0°/6.0°/5.5°/8.0°. Neither validation-agent.md nor any code-adjacent spec establishes this as the intended Hellenistic orb scheme; traditional Hellenistic practice uses sign-based (whole-sign) aspects rather than degree-based orbs. If degree-based orbs are retained as a separate channel, the policy should probably allow wider orbs for aspects to the luminaries (documented as 9–12° in most modern traditions). This is the source of the "missing" Sun-Venus and Sun-Saturn aspects for marwa.
   Recommendation: Decide explicitly whether the "mainAspects" channel is intended as Hellenistic sign-based or as modern orb-based, and if the latter, widen luminary orbs. Not a correctness bug against the current table, but a convention question the project should pin down.

=== SPOT-CHECK VERIFICATIONS (manual formulas applied to reda, a diurnal chart) ===

1. Chart: reda; Field: Eros (diurnal, per validation-agent.md: Asc + Venus − Spirit)
   Manual: 286.54 + 167.82 − 121.95 = 332.41 → Pisces 2.41°
   Mystro output: Sagittarius 0.67°  →  AGREEMENT: NO (error = 180° − 2·Asc + 2·Spirit = 91.74° in the ecliptic, sign-reversed)
   Astro-Seek output: Pisces 2.40°  →  AGREEMENT: YES

2. Chart: reda; Field: Necessity (diurnal, per validation-agent.md: Asc + Mercury − Fortune — NOTE: validation-agent.md lines 120-121 state this as the diurnal form; Brennan gives Asc + Fortune − Mercury. They disagree. Using Brennan/Paulus because it is the standard cited in the doc's own §"Reference source" language.)
   Manual (Brennan diurnal = Asc + Fortune − Mercury): 286.54 + 91.13 − 188.21 = 189.46 → Libra 9.46°
   Mystro output: Aries 23.62°  →  AGREEMENT: NO
   Astro-Seek output: Libra 9.48°  →  AGREEMENT: YES
   NB: The validation-agent.md formula table at lines 119-124 is internally inconsistent with the later textual reference to Brennan/Paulus at line 124; the project should pick one canonical formula set and update validation-agent.md to match.

3. Chart: reda; Field: Courage (diurnal, Asc + Mars − Fortune — both sources agree on this direction)
   Manual: 286.54 + 168.50 − 91.13 = 363.91 → 3.91° Aries
   Mystro output: Aries 3.91°  →  AGREEMENT: YES
   Astro-Seek output: Libra 29.18°  →  AGREEMENT: NO (Astro-Seek applied nocturnal formula)

4. Chart: marwa; Field: Courage (nocturnal, per Brennan: Asc + Fortune − Mars)
   Manual: 158.30 + 194.77 − 332.20 = 20.87 → Aries 20.87°
   Mystro output: Capricorn 25.73°  →  AGREEMENT: NO
   Astro-Seek output: Capricorn 25.72°  →  AGREEMENT: NO (shared error — MATCH in report.json is misleading)

5. Chart: marwa; Field: Antiscion of Ascendant (axis Cancer↔Capricorn, λ=158.30° Virgo)
   Manual: 180 − 158.30 = 21.70 → Aries 21.70°
   Mystro output: Aries 21.70°  →  AGREEMENT: YES

6. Chart: marwa; Field: Contra-antiscion of Ascendant (axis Aries↔Libra)
   Manual: 360 − 158.30 = 201.70 → Libra 21.70°
   Mystro output: Libra 21.70°  →  AGREEMENT: YES

7. Chart: marwa; Field: Dodecatemorion of Sun (Sun at Sag 19.95°, Valens: 0° Sag + 12·19.95°)
   Manual: 240° + 239.40° = 479.40° → 119.40° = Cancer 29.40°
   Mystro output: Cancer 29.44°  →  AGREEMENT: YES (within 0.04°, consistent with internal precision > 2 decimals)

8. Chart: marwa; Field: Planetary hour (2001-12-11 Tuesday, day ruler Mars; Chaldean Sun→Venus→Mercury→Moon→Saturn→Jupiter→Mars sequence starting at the day ruler)
   Expected day ruler: Mars (Tuesday); expected hour-1 ruler = Mars.
   Mystro output: dayRuler=Mars, 18th hour of Day, hourRuler=Mercury. Sequence from hour 1 Mars: Mars, Sun, Venus, Mercury, Moon, Saturn, Jupiter, Mars, Sun, Venus, Mercury, Moon, Saturn, Jupiter, Mars, Sun, Venus, Mercury → hour 18 = Mercury. AGREEMENT: YES.

=== DOMAINS NOT EXERCISED BY THIS RUN ===
- Lord of the Orb: no processor emits this section; not in any output JSON. The Config.CHALDEAN_YEAR_SEQUENCE constant exists but is unused.
- Southern-hemisphere / high-latitude / sign-boundary / station edge cases: none of ilia/marwa/reda hit these. To cover the validation-agent's Edge Cases list, add at least one southern-hemisphere chart and one > 66° latitude chart to native-list.json.
- Quadrant-house validation: all three charts specify Whole Sign. If other systems are in scope, add a Placidus chart.

=== END OF REPORT ===
