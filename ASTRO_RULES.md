# Astro Rules and Inferred Algorithms

Last updated: 2026-04-22

This file stores astrology-specific rules and inferred algorithms adopted in the `mystro` project so future sessions can continue without reconstructing them from scratch.

## 1. Lord of the Orb

### Goal
Match the Astro-Seek style Lord of the Orb logic as closely as possible.

### Algorithm
1. Compute the **planetary hour ruler at birth** using the actual birth data from the input file.
2. The birth planetary hour ruler is the Lord of the Orb for the **first year of life / age 0 row**.
3. Advance one planet per year in **Chaldean order**:
   - Saturn → Jupiter → Mars → Sun → Venus → Mercury → Moon
4. Support two variants:
   - **Mod 84**: continuous 7-planet cycle
   - **Mod 12**: same progression, but reset every 12 years

### Notes
- This replaced an older placeholder implementation based on term/triplicity.
- The correct source is the **birth planetary hour ruler**, not the profected sign's term/triplicity lords.
- Saved Astro-Seek HTML currently exposes only the visible 12-row Lord of the Orb table window, so validator comparisons should match overlapping ages rather than assuming a full 0..83 export.

## 2. The 7 Hermetic Lots

### Goal
Produce results approximately matching Astro-Seek for the 7 principal Hermetic Lots.

### Lots used
- Fortune
- Spirit
- Eros
- Victory
- Necessity
- Courage
- Nemesis

### Formula conventions
The project now uses explicit sect-conditional formulas for all seven lots.

Arrow notation follows the textbook convention used in Dorotheus / Valens / Brennan:
- `A → B` means the forward arc from `A` to `B`
- mathematically: `(B - A)`
- lot longitude = `Asc + (B - A)`

For **diurnal** births:
- Fortune = Sun → Moon
- Spirit = Moon → Sun
- Eros = Spirit → Venus
- Victory = Spirit → Jupiter
- Necessity = Mercury → Fortune
- Courage = Fortune → Mars
- Nemesis = Saturn → Fortune

For **nocturnal** births:
- Fortune = Moon → Sun
- Spirit = Sun → Moon
- Eros = Venus → Spirit
- Victory = Jupiter → Spirit
- Necessity = Fortune → Mercury
- Courage = Mars → Fortune
- Nemesis = Fortune → Saturn

### Important implementation note
Implementation should keep the arc direction explicit in code and formulas. `HermeticLotsProcessor.projectLot()` currently computes `Asc + p1 - p2`, so the displayed textbook formula label for that computation is `p2 → p1`. Astro-Seek agreement is useful, but shared Courage mismatches in saved HTML mean parity alone is not enough for validation.

## 3. House logic for Hermetic Lots

### Rule
For Hermetic Lots, use **whole-sign house by sign**, not floating/object house placement.

### Reason
This gave results much closer to Astro-Seek than `chart.houses.getObjectHouse(...)` for the lots.

### Formula
If the Ascendant sign is house 1, then the lot house is:
- the number of signs from Asc sign to lot sign, inclusive

Example with Pisces rising:
- Aries = 2
- Taurus = 3
- Gemini = 4
- Cancer = 5
- Virgo = 7
- Libra = 8
- Sagittarius = 10

## 4. Lot-to-ruler relationships

### Columns
- **L→R** = relation from the lot sign to the ruler's sign
- **F→R** = relation from Fortune's sign to the ruler's sign

### Rule
These are computed by **whole-sign sign relationship**, not degree-based aspect geometry.

### Output style
- Return ordinal house-style distances like:
  - `4e`
  - `5e`
  - `10e`
  - `11e`
- Use `A` for aversion cases.

## 5. Meaning of `A`

### Rule
`A` means **Aversion**.

### Aversion sign distances
Use `A` when the target sign is the:
- 2nd
- 6th
- 8th
- 12th
from the source sign

In zero-based modular offsets, these are:
- 1
- 5
- 7
- 11

This matched Astro-Seek-style results much better than interpreting `A` as angular.

## 6. Future guidance

When refining astro algorithms in future sessions:
1. Prefer matching **algorithmic behavior** over copying display text from Astro-Seek.
2. When outputs differ slightly in degrees, first preserve the structural logic:
   - formulas
   - house assignment method
   - sign relations
   - aversion handling
3. Save any confirmed Astro-Seek-oriented inference here so it survives interrupted sessions.
