# CLAUDE.md — StreamKit

Personal Android/Fire TV streaming reference app. Single-user, no public distribution — learning project + portfolio for streaming-company roles. Package: `com.streamkit`.

## Spec-Driven Development — read before writing code
Specs are written before implementation and are the source of truth. Before touching code for any feature:
1. Read `CONTEXT.md` first, every session — current state, open questions, recent decisions.
2. Read `specs/features/<feature>/{requirements,design,tasks}.md` for the feature in progress.
3. Cross-check `ARCHITECTURE.md`, `SPEC.md`, `specs/technical/data-model.md`, and `specs/design/design.md` for anything the feature spec touches.

If a task file conflicts with `data-model.md`, `ARCHITECTURE.md`, or another spec, stop and flag it — don't silently resolve it.

## Repo structure
Single Android Gradle project in `android/` (`core`, `app`, `tv` modules) + `backend/` (FastAPI, inactive until Phase 4) + `specs/`. 
Module rules: `app → core`, `tv → core`, never `app ↔ tv`. Full layout in `ARCHITECTURE.md`.

## Conventions
- Business rules: cite `BR-[FEATURE]-[##]` IDs from `SPEC.md` — never restate rule text in other docs.
- Tasks are scoped to single files or tightly-related pairs (`specs/features/*/tasks.md`) — implement one task at a time.
- Author name in generated docs: "Danielle Mariani".
- Changelogs are newest-first.

## Workflow
Work task-by-task from `specs/features/<feature>/tasks.md`. 
Before starting: confirm `Depends on` tasks are done. 
After finishing: update task status; flag any knock-on doc updates rather than making them silently.