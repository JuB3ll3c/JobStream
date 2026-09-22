# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Before exploring, read these

- `CONTEXT.md` at the repository root.
- `docs/adr/` for decisions related to the area being changed.

If these files do not exist, proceed silently. Do not create them upfront. The `/domain-modeling` skill creates them lazily when terminology or decisions are resolved.

## File structure

This is a single-context repository:

```
/
├── CONTEXT.md
├── docs/
│   └── adr/
├── api/
└── front/
```

## Use the glossary's vocabulary

Use terms as defined in `CONTEXT.md` in issue titles, specifications, hypotheses, tests, and code. Do not substitute synonyms explicitly rejected by the glossary.

If a required concept is missing, reconsider whether it belongs to the domain or note the gap for `/domain-modeling`.

## Flag ADR conflicts

Surface any contradiction with an existing ADR instead of silently overriding it.
