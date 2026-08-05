# Mystro Project Vision

> Documentation axis: why Mystro exists and what belongs inside the product.

## Purpose

Mystro produces auditable calculated evidence for traditional-astrology readings. The user
supplies birth or event data, Mystro performs the astronomical and traditional calculations, and
the resulting structured data can then be interpreted outside Mystro.

Mystro's responsibility ends with calculated evidence. Psychological framing, prediction, advice,
and conversational narrative belong to the user or to an external assistant chosen by the user.

## Audience

The intended user may ask ordinary life questions while leaving the selection of historical
techniques to Mystro. Mystro therefore organizes calculations by reading task.

## Task-first doctrine

The analytical question determines the apparatus. A doctrine is a hardcoded historical knowledge
module that supplies coherent calculation choices, tables, and techniques for a task.

A reading task has:

- one primary traditional apparatus;
- explicitly labelled compatible annexes;
- calculated output shaped for that task;
- coherent and explicit conventions.

The canonical task taxonomy is defined only in
[READING_TASKS_SPEC.md](READING_TASKS_SPEC.md).

## Evidence boundary

Mystro distinguishes three responsibilities:

1. astronomical facts, such as positions, houses, time scales, and event instants;
2. traditional calculations, such as lots, dignities, chronocrators, and directions;
3. interpretation, which remains downstream.

Machine output contains the first two. Explanatory prose belongs in documentation or
human-readable research reports.

## Product principles

- Offline-first command-line operation.
- Explicit inputs and reproducible calculations.
- Small calculators and explicit data models.
- Full internal numerical precision; presentation layers may round.
- Source or method labels where a result depends on a doctrine.
- Fail-fast behavior when a required astronomical calculation is unavailable.
- Synthetic committed fixtures and private local natal input.

## Scope boundary

Mystro grows by implementing one complete reading task at a time. Each new technique answers a
defined task, has an explicit calculation contract, and can be tested. Progress is measured by
coherence and verifiability.
