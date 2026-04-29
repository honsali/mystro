# Astrology Rules

Universal rule notes are intentionally minimized because rules belong inside doctrine modules.

The authoritative architecture is:

- `NEW_ARCHITECTURE_SPEC.md`

## Current rule philosophy

A doctrine is a hardcoded knowledge module.

Each doctrine defines:

- what exists for that doctrine
- which concepts are meaningful
- which techniques it exposes
- which calculations it performs
- which result shape best represents its own logic

The engine does not maintain one universal astrology rule schema.

Rules such as lots, dignities, aspects, profections, directions, returns, synastry, or other techniques belong inside the doctrine module that exposes them.

If a concept is not meaningful or not calculated by a doctrine, it is absent from that doctrine's result.
