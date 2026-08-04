# Mystro Project Vision

## Purpose

Mystro produces structured traditional-astrology chart data for use outside Mystro. A user provides birth or event data, Mystro computes coherent calculated data, and the user brings that data to an AI assistant of their choice for conversational interpretation.

The current product is a command-line JSON reading bundle. For large local/research life-arc work, Mystro also produces Markdown research reports because they are more compact and readable for human and AI analysis than enormous JSON timelines. In both modes, Mystro's product is calculated evidence; the conversation, narrative synthesis, ethical framing, and final explanation happen downstream in the user's chosen assistant.

## Audience

Mystro is designed for non-specialist users who ask human questions rather than technical astrology questions:

- "Why do I struggle in love?"
- "What kind of career path fits me?"
- "What does this say about my family?"
- "What period of life am I in?"

The output must be technically rich enough for a capable AI assistant to answer those questions from grounded chart data, while keeping doctrinal disputes and specialist terminology organized rather than exposed as noise.

## Core product principle

Mystro is task-first.

The analytical objective determines the technical apparatus. A house system, timing method, lot family, or master is selected because it belongs to the reading task being produced. Lilly is not a better or worse Valens; Lilly belongs to a different style of question and procedure.

Each reading task has:

- a primary tradition or master that gives the task its backbone
- a coherent technical apparatus
- additive annexes from other masters when they enrich the task without breaking coherence
- a structured output shape designed for downstream AI interpretation, usually JSON for public/machine contracts and Markdown for large local research reports

## Doctrine model

A doctrine is a hardcoded historical knowledge module. It supplies calculation choices, tables, techniques, and doctrine-specific output atoms.

A doctrine is not a user profile, configuration preset, or interchangeable style setting. Users request a reading task; Mystro selects the appropriate primary apparatus for that task and may add clearly labelled annexes.

Example: the current Natal Description reading uses a Valens/Hellenistic backbone and adds compatible material such as Ptolemaic hyleg/alcocoden vitality output, Dorothean family lots, Manilius-inspired sign lore, and an explicitly parameterized fixed-star conjunction catalogue.

## Output design

Mystro's command-line JSON is designed to be pasted or uploaded into a general AI assistant. The JSON remains pure calculated data; field explanations, caveats, and reading guidance belong in documentation or local Markdown reports rather than in calculated chart fields.

For local/research life-arc calculators, Markdown reports under `output/<alias>/` are currently the preferred AI-facing format. The Java calculator records/classes remain the canonical calculation model; Markdown is the readable report layer for large timing tables, active-period summaries, and evidence grouping. JSON should be added for those research calculators only when a stable machine contract is explicitly needed.

Design rules:

- stable, explicit field names or table columns
- source/doctrine labels where a technique belongs to a particular tradition
- formulas or method labels for computed values when useful
- clear separation between astronomical facts, traditional calculations, and downstream interpretation
- no REST-only response scaffolding or server-served companion resource
- explanatory prose, warnings, justifications, and reading suggestions kept out of calculated chart JSON and maintained in documentation/local Markdown files when needed

## Conceptual layers

Each reading bundle can be understood through four layers.

### Layer 1 — Astronomical data

Swiss Ephemeris-backed astronomical facts: positions, speeds, declinations, houses, angles, sect baseline, and time/location metadata.

### Layer 2 — Shared traditional mechanics

Traditional calculations that are broadly reusable across tasks: zodiacal signs, domiciles, exaltations, antiscia, aspect helpers, lots where common, sect-related primitives, and similar mechanical structures.

### Layer 3 — Task-specific doctrine

Technique sets selected for a reading task: Valens-style natal description, Lilly-style horary judgment, Dorothean elections, mundane ingress work, medical decumbiture analysis, and so on.

### Layer 4 — Interpretation

Narrative synthesis and advice produced by the external AI assistant using Mystro's JSON and/or local Markdown reports as evidence.

## Canonical reading tasks

Mystro's traditional-astrology scope is organized into six reading tasks:

1. Natal description — who this person is by nature
2. Life-arc prediction — how the natal promise unfolds through time
3. Horary / event prediction — judgment of a specific question or event
4. Elections — choosing an appropriate moment for an action
5. Mundane prediction — places, rulers, weather, politics, dynasties, and collective events
6. Medical astrology — illness, temperament, crisis days, and treatment timing within traditional astrological medicine

Mystro implements one reading task at a time, then expands it through focused vertical slices. The implemented command-line JSON baseline task is Natal Description. Life-arc prediction/module-2 timing is currently produced as local/research Markdown reports for annual/monthly profections, compact all-lots L1 Zodiacal Releasing plus per-lot Zodiacal Releasing, firdaria, decennials, solar returns, solar-return-to-natal comparison, distributions through bounds, primary-direction variants, lunar timing with eclipse evidence, active evidence synthesis, topic synthesis packets, an AI brief, and an output index. Transit work is kept out of the 0-100 macro dump and belongs to the bounded high-zoom pack under `output/<alias>/<yyyyMMdd>/`.

## Engineering posture

Mystro favors small calculators, explicit data models, and auditable output. Each emitted calculator should correspond to a recognizable chart-specific analytical concept: lots, doryphories, hyleg, triplicity life phases, fixed-star conjunctions, chronocrator periods, return charts, transit activations, and similar atoms. Static doctrine/lore tables such as sign characters, sign-level paranatellonta, and sign melothesia belong in documentation or local Markdown rather than per-chart JSON.

Calculators should keep full internal precision and leave JSON rounding to serialization. Markdown renderers may round for readability, but calculators should retain full precision. Missing optional inputs should be handled deliberately so that incomplete optional sections can be omitted or reduced without corrupting the whole reading bundle or local research report.
