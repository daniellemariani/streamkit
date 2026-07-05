---
globs: android/**
---
# Android Conventions — StreamKit

Applies to any file under `android/` (`core`, `app`, `tv` modules).

## Stack
Kotlin, Jetpack Compose (`app`), Compose for TV (`tv`), MVI + Repository, Hilt DI, Room, Ktor Client, Coroutines/Flow. Min SDK 31, target/compile SDK 35. Package `com.streamkit`.

## Module rules
- `app → core`, `tv → core`. Never `app ↔ tv`. `core` has no UI and no knowledge of `app` or `tv`.
- Composables never call a Repository or UseCase directly — always through a ViewModel.
- Repositories are interfaces in `core/domain/repository/`; implementations in `core/data/repository/`.

## MVI pattern
Every screen: one `UiState` data class, one `Event` sealed class (user/player intents), one `UiEffect` sealed class (one-time side effects like navigation) emitted via a buffered `Channel`. The ViewModel is the only place state mutates — Composables never mutate state directly.

## Testing
- Unit tests: JUnit + Turbine for Flow testing. Hand-written fakes for repositories/use cases — no Mockito.
- Room: `Room.inMemoryDatabaseBuilder`, fresh instance per test, instrumented (`androidTest`).
- UI tests are deferred for this project (`ARCHITECTURE.md` Testing Strategy) — don't add Compose UI tests unless asked.

## Design tokens
Colors, spacing, and typography come from `specs/design/design.md`, mapped to Material 3 in `core/ui/theme/`. Never hardcode hex colors or raw `sp`/`dp` values in a composable — reference the theme.