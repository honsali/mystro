# Spec: Build This App From Scratch

## What you are building
Build a small standalone Java app called **Mystro**.

This app is for **traditional astrology**.
Its job is to:
- read saved birth data from JSON
- calculate astrology results for each person
- read matching saved Astro-Seek HTML pages
- extract the same astrology sections from those pages
- save both sides as JSON
- create one final comparison report

The purpose is **not** to make a website, a mobile app, or a big product.
The purpose is to make a **local comparison tool** that helps check whether Mystro gives results close to **Astro-Seek**.

Think of it as a batch processor:
1. input goes in
2. calculations happen
3. Astro-Seek saved pages are parsed
4. both results are saved in the same format
5. differences are listed

## Very important package rule
Use these two repositories as the Swiss Ephemeris base:

- **Core package:** https://github.com/kim-dam-petersen/swisseph-module
- **Wrapper project:** https://github.com/krymlov/swe-java-lib

Required meaning:
- use **swisseph-module** for the low-level ephemeris core
- use **swe-java-lib** as the wrapper layer

Do not replace them with another astrology engine.

## Main goal
The app must rebuild the same overall behavior as the current Mystro workflow, but assume you know nothing in advance.
So here is the complete expected behavior.

## What the app should do, in simple words
The app receives a list of people.
For each person, it should do two parallel jobs:

### Job 1: Mystro side
Use the person’s birth data to calculate astrology results.

### Job 2: Astro-Seek side
Open a saved Astro-Seek HTML page for that same person and extract the same astrology results from the page.

### Final step
Save both results as JSON and compare them.

## Why this exists
The main purpose is to check whether Mystro calculations match Astro-Seek as closely as possible.

So when something is unclear, choose the behavior that best matches saved Astro-Seek examples.

## What kind of app this is
Build it as:
- a **Java** application
- run from the command line
- self-contained
- English-only
- JSON-only

Do **not** build:
- a web app
- a GUI-first app
- a multilingual app
- a markdown report generator
- a database system
- a live Astro-Seek scraper

## How the app is started
The app should have **one main entry point**.

It must support this behavior:
- if names are passed in the command line, process only those names
- if no names are passed, process all people from the input file

So the app must support:
- running selected cases
- running all cases

## Input data the app must read
Use one JSON file containing a list of people.
A good file path is:
- `input/native-list.json`

Each person entry must contain:
- `name`
- `birth_date`
- `birth_time`
- `latitude`
- `longitude`
- `utc_offset`
- `house_system`
- `zodiac`
- `terms`

### Expected formats
Use these formats exactly:
- `birth_date`: `dd/MM/yyyy`
- `birth_time`: `HH:mm`
- latitude and longitude: decimal numbers
- `utc_offset`: explicit text like `+01:00` or `+00:00`

### Expected astrology settings
Assume these values are the standard target unless a case says otherwise:
- `house_system`: `Whole Sign`
- `zodiac`: `Tropical`
- `terms`: `Egyptian`

## Example input entry
```json
{
  "name": "ilia",
  "birth_date": "14/07/1975",
  "birth_time": "22:55",
  "latitude": 50.60600755996812,
  "longitude": 3.0333769552426793,
  "utc_offset": "+01:00",
  "house_system": "Whole Sign",
  "zodiac": "Tropical",
  "terms": "Egyptian"
}
```

## Astro-Seek data source
The app must **not** call Astro-Seek online.
It must use **saved HTML files** already stored locally in the project.

This is important.
The app is a local processor, not a live website scraper.

### Important Astro-Seek rule
When reading the Astro-Seek side:
- parse the birth details from the saved Astro-Seek HTML itself
- do not simply copy the birth details from the input JSON

That rule matters because the Astro-Seek side must represent what the page actually says.

## What each run should do
For every selected person, the app should:
1. find that person in the input JSON
2. calculate the Mystro result from the birth data
3. locate the matching saved Astro-Seek HTML file
4. parse the same result sections from that HTML
5. convert both sides into the same JSON shape
6. save both JSON files
7. compare the two JSON files
8. store the comparison result in the final summary

If one person fails, the app should continue with the others whenever possible.

## Output files the app must produce
For each processed person, write:
- one Mystro JSON file
- one Astro-Seek JSON file

Also write:
- one global comparison JSON file for the whole run

Recommended output structure:
- `output/mystro/json/<name>.json`
- `output/astroseek/json/<name>.json`
- `output/report.json`

## What must be inside the JSON outputs
Both Mystro and Astro-Seek outputs should be normalized into the same general structure so they are easy to compare.

At minimum, both should contain these sections whenever the data exists.

### 1. Birth
This section should contain the person’s birth details used by that side.

Include the usual fields such as:
- name when available
- birth date
- birth time
- latitude
- longitude
- UTC offset
- house system
- zodiac
- terms

## 2. Planetary Hour
Include the planetary hour information.

Keep fields such as:
- summary text if available
- mode
- hour number
- day ruler
- night ruler if available
- hour ruler
- start time if available
- end time if available

## 3. Syzygy
Include:
- phase
- date and time

## 4. Hermetic Lots
Include these seven lots:
- Fortune
- Spirit
- Eros
- Victory
- Necessity
- Courage
- Nemesis

For each lot, keep:
- lot name
- lot sign
- lot degrees
- lot house
- ruler
- ruler sign
- ruler degrees
- ruler house
- lot-to-ruler relation
- fortune-to-ruler relation when relevant

## Astrology target behavior
The target behavior is **Astro-Seek**.
That means:
- use Astro-Seek as the reference behavior
- if your result and Astro-Seek differ, the difference should appear in the report
- if a rule is uncertain, prefer what best matches saved Astro-Seek examples

## Lord of the Orb rule
If you implement Lord of the Orb, use this exact approach:

1. Start from the **birth planetary hour ruler**.
2. Move forward year by year in **Chaldean order**:
   - Saturn → Jupiter → Mars → Sun → Venus → Mercury → Moon
3. Support both modes:
   - **Mod 84**: continuous 7-planet cycle
   - **Mod 12**: same progression, but reset every 12 years

## Fortune row direction rule
For the Fortune row, keep the direction as:
- `L→R`

## How Astro-Seek parsing should be organized
Do not create one giant parser.
Use several small parsers and one coordinator.

At minimum, have separate parsing parts for:
- birth data
- planetary hour
- syzygy
- Hermetic lots

This makes the app easier to maintain and easier to compare section by section.

## How Mystro calculation should be organized
Do not put all astrology logic in one huge class.
Split it into small focused calculation parts.

At minimum, separate:
- birth data normalization
- planetary hour calculation
- syzygy calculation
- Hermetic lots calculation

## Comparison behavior
The app must compare the Mystro result and the Astro-Seek result for each person.

Each person should get a final status, such as:
- `MATCH`
- `MISMATCH`
- `MISSING_INPUT`
- `MISSING_ASTROSEEK_HTML`
- `MISSING_OUTPUT`

The final summary file should include:
- requested names
- total requested
- how many were compared
- how many matched
- how many mismatched
- missing input list
- missing Astro-Seek HTML list
- missing Mystro output list if needed
- missing Astro-Seek output list if needed
- one result entry per person

Each person entry should include:
- the person name
- the final status
- a list of differences written in simple readable text

## Difference writing style
Differences should be easy to read.
For example, it should be clear which field differs and what each side says.

The report does not need to be fancy.
It just needs to be clear and useful.

## Error handling rules
The app should be tolerant.
Do not stop the whole run just because one item fails.

Collect problems and continue when reasonable.

Examples of issues to collect:
- a requested name is not found in the input JSON
- the Astro-Seek HTML file is missing
- one parser section fails
- one output file cannot be written
- one comparison cannot be completed

## Project shape to build
Keep the project simple.
A good structure is:
- one main app entry point
- one input loader
- one Mystro service for calculations
- one Astro-Seek service for parsing
- one comparison service
- shared models for the normalized output
- one low-level Swiss Ephemeris support layer that uses the required repositories

## What not to add
Do not expand the project into something else.
Avoid adding:
- markdown output
- translation support
- accounts or users
- databases unless truly necessary
- web APIs unless truly necessary
- live web scraping from Astro-Seek
- extra UI layers

## Validation checklist
The rebuild is acceptable only if it can do all of the following:

1. compile successfully
2. start from one main command-line entry point
3. read a JSON list of people
4. process selected names from the command line
5. process all names when no names are given
6. calculate Mystro results from birth data
7. parse Astro-Seek results from saved local HTML
8. save Mystro JSON files
9. save Astro-Seek JSON files
10. save one final comparison JSON report
11. continue running even if one case is missing or fails
12. show readable mismatch details
13. stay English-only
14. stay JSON-only
15. use `swisseph-module` as the core package
16. use `swe-java-lib` as the wrapper project

## Priority order
If trade-offs are needed, use this order:

1. reproduce the same overall app behavior
2. keep the same input/output workflow
3. keep the same normalized JSON idea
4. keep the same comparison workflow
5. match Astro-Seek behavior as closely as possible
6. keep the structure simple and maintainable

## Final short version
Build a Java command-line app that:
- reads people from JSON
- calculates astrology data using the required Swiss Ephemeris core and wrapper
- reads saved Astro-Seek HTML pages
- parses the same sections from Astro-Seek
- saves both sides as JSON
- compares them
- writes one final JSON report
- stays simple, local, English-only, and JSON-only
