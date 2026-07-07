# Design System — StreamKit

**Version:** 0.1.2
**Status:** Draft
**Owner:** Danielle Mariani
**Created at:** 2026-06-26
**Last Updated:** 2026-07-07
**Location:** specs/design/design.md

---

## Overview

This document defines the visual design system, UX principles, theming strategy, and cross-platform UI guidelines for StreamKit.

The goal of the design system is to create a calm, cinematic, instrument-panel-precise experience that scales consistently across Android mobile (`app`) and Fire TV (`tv`).

This document acts as the canonical visual and UX reference for the product. All feature specifications, component implementations, and platform-specific UI layers must align with the principles and constraints defined here.

This document is intentionally implementation-agnostic. It defines visual and interaction standards, not platform-specific code.

---

## Related Documents

| Document | Purpose |
|---|---|
| SPEC.md | Global business rules and feature index |
| PRODUCT.md | Product vision and roadmap |
| ARCHITECTURE.md | Technical architecture and platform decisions |
| navigation.md | Navigation flows and screen relationships — the canonical source for which screens and components exist |

---

## Goals

- Establish a single dark theme — no light theme — reused identically across Android mobile and Fire TV
- Keep the UI quiet enough that posters, video, and live content stay the visual focus, not the chrome around them
- Make the Settings diagnostics overlay (bitrate, resolution, buffer health, dropped frames, network type) legible and precise without turning the player into a dashboard
- Establish a consistent visual identity between the touch-first mobile app and the D-pad-first, 10-foot Fire TV app
- Define a reusable color system and typography scale
- Standardize spacing, tonal elevation, and component behavior
- Prevent design drift as Phases 2–6 add Cast, Fire TV, DRM, and Telemetry UI

---

## Non-Goals

- Define implementation-specific Compose widgets (component code lives in `core`/`app`/`tv`, per `ARCHITECTURE.md`)
- Replace Material 3 (Android) or Compose for TV's lean-back conventions — those remain the platform foundation
- Provide pixel-perfect mockups for every screen
- Define a light theme — dark-only is a deliberate decision, not a placeholder for one (see Design Philosophy)
- Define marketing or promotional branding assets — StreamKit has no public distribution (`PRODUCT.md` non-goals)
- Define illustration systems or mascot/brand character guidelines
- Define web design tokens — there is no web client in StreamKit's scope; the Phase 6 telemetry dashboard is a developer tool, not a design-system surface (see Platform Adaptation)

---

## Design Philosophy

StreamKit is designed around the concept of:

> "The signal, not the noise."

The product should help the viewer feel like the interface gets out of the way of the content — and, in the Settings overlay, that the underlying streaming mechanics (bitrate, buffer, network) are presented with the same calm precision as a broadcast monitor, not a cluttered dashboard.

The app should feel:

- Calm
- Cinematic
- Instrumented — confident and precise where it shows technical detail, never noisy about it
- Unobtrusive
- Consistent between mobile and TV

The app should avoid:

- Decorative gradients or glow effects
- Color used for anything other than its one defined meaning (interactive, live, warning, error)
- Visual competition with the video surface itself
- Playful or bouncy motion
- Dense, dashboard-like overlays that fight the content for attention

The experience should emphasize clarity, restraint, and long-term consistency over novelty — fitting `PRODUCT.md`'s own principle that this is a learning project where "a working ABR player with a plain UI is better than a beautiful app with fake playback."

---

## Design Principles

### Content is the hero, chrome recedes

UI elements exist to support viewing, not to compete with it. Poster art and the video surface get full visual priority; controls, labels, and metadata stay quiet — dark, restrained, low-contrast against themselves — until the viewer needs them.

In practice: player controls sit on a translucent dark scrim only when shown, never a fully opaque bar; catalog cards are mostly poster image with minimal surrounding chrome.

---

### Signal-accent, not decoration

The accent (cyan) and the live/error color (broadcast red) carry meaning only. Accent and semantic colors should be reserved for:

- Interactive elements (selected tab, progress fill, focused control)
- The Live badge and DVR live-edge indicator
- Buffering / degraded-network warning states
- Error states

Using either color outside its defined role dilutes its signal value — if everything is highlighted, nothing is.

---

### Tonal elevation over shadow

Since the canvas is near-black, elevation is communicated through progressively lighter surface tones (Material's dark-theme tonal elevation approach), not drop shadows — shadows barely render against a background this dark and would look like dirt rather than depth.

Practical guidance: when something needs to read as "above" the page (a card, a sheet, a dialog), lighten its surface tone. Don't reach for `box-shadow`/elevation-via-shadow as the first tool on this theme.

---

### Consistent identity, native interaction

Android mobile and Fire TV should feel like members of the same product family. Color, typography, spacing philosophy, and component meaning stay identical; only the interaction model and information density differ — touch vs. D-pad, arm's-length vs. 10-foot viewing distance.

---

### Cross-Platform Consistency

`app` and `tv` should feel like the same product. Platform-specific conventions may differ (D-pad focus vs. touch ripple, denser mobile layouts vs. larger lean-back text), but typography, colors, layout philosophy, spacing, semantic meaning, and component behavior must remain consistent.

---

## Visual Identity

### Overall Style

The visual style is dark, cinematic, technical, and restrained.

The UI should use:

- Full-bleed poster art as the primary visual content on Catalog
- Tonal dark surfaces with a single accent hue
- Monospace numerals for diagnostic readouts, so values don't jitter the layout as they update in real time
- Generous negative space around the video surface — nothing crowds the frame
- A functional scrim (a dark gradient strictly behind overlaid text on poster art, for legibility) — this is the one place a gradient is permitted, because it solves a real contrast problem rather than decorating

The UI should avoid:

- Decorative gradients, glassmorphism, or glow/neon effects anywhere else
- Heavy shadows (ineffective against near-black; see Tonal Elevation principle)
- Saturated multi-color decoration
- Dense overlays that compete with the video for attention

---

### Reference Applications

- **Netflix** — calm dark chrome and a poster-forward catalog; the clearest existing example of "content is the hero"
- **YouTube** — player control conventions this app already commits to in `navigation.md`: invisible double-tap seek zones, long-press-to-speed, PiP behavior
- **Broadcast monitor / waveform-scope UIs** — the visual reference for the Settings diagnostics overlay specifically: calm, monospaced, signal-colored readouts rather than a dashboard-style panel. This is the more unusual reference, and it's deliberate — it's what makes the diagnostics overlay feel like real engineering instrumentation rather than a bolted-on debug screen.

These references are directional inspiration only. StreamKit maintains its own identity.

---

## Color System

### Color Philosophy

The color system is intentionally restrained: one dark tonal scale for structure, one accent hue for everything interactive or "alive," and broadcast red reserved for the two states that genuinely need urgency — live content and errors.

The accent uses a cyan-teal rather than a color any major streaming service already owns (Netflix red, Disney+ blue, Hulu green, HBO Max purple, Peacock's multicolor mark). Cyan-teal reads as a broadcast/waveform-monitor signal color — directly tied to what this app is actually demonstrating technically (ABR, signal health) — rather than borrowing a competitor's brand identity.

---

### Theme

StreamKit is **dark-only by deliberate decision**, not a placeholder for an eventual light theme — see Non-Goals. There is no Light Theme table in this document because none exists to define.

| Role | Token | Hex |
|---|---|---|
| Background | `color.background.primary` | `#0B0D10` |
| Surface / Card | `color.surface.card` | `#16191D` |
| Surface Alt (high elevation) | `color.surface.alt` | `#1F2329` |
| Border | `color.border.default` | `#2A2F36` |
| Primary Text | `color.text.primary` | `#F4F5F6` |
| Secondary Text | `color.text.secondary` | `#93999E` |
| Primary Accent | `color.accent.primary` | `#2EE6C7` |
| On Primary Accent | `color.accent.on` | `#04201C` |
| Success | `color.semantic.success` | `#2EE6C7` *(alias of accent — see note)* |
| Error / Live | `color.semantic.error` | `#FF3B3F` |
| Warning | `color.semantic.warning` | `#FFB454` |
| Info | `color.semantic.info` | `#2EE6C7` *(alias of accent — see note)* |

**Note on Success and Info:** rather than introduce two more hues for states this app rarely surfaces (there's no meaningful distinct "info" notification pattern, and the closest thing to "success" — an ingest job completing, a cast session connecting — reads naturally as "healthy," the same meaning the accent already carries), Success and Info are intentionally aliased to the same accent hex. This is a deliberate consolidation in line with the restraint principle above, not an oversight — if a genuinely distinct success/info use case shows up in a later phase, it gets its own hex then, not preemptively now.

`color.border.default` (`#2A2F36`) wasn't part of the palette reviewed earlier — it's a low-contrast hairline one step above Surface Alt, needed for things like the Settings list dividers and input borders that the original 8-swatch review didn't need to cover.

---

### Dark Mode Strategy

- This is not "dark mode" as an alternate theme — it's the only theme, supported from Phase 1.
- Elevation is communicated via the three tonal steps (`background.primary` → `surface.card` → `surface.alt`), each progressively lighter, never via shadow.
- The accent hue is single and constant — there's no separate light-mode accent to keep in sync.
- Semantic colors keep their meaning everywhere they appear; there's no mode-dependent shade adjustment needed since there's only one mode.

---

### Color Independence Rule

Color alone must never be the sole signal communicating a meaningful state. Every color-coded state also includes at least one of:

| Secondary signal | Examples |
|---|---|
| Label / text | The Live badge always shows the word "Live," never just a colored dot |
| Icon | Error states pair the red with an error icon, not color alone |
| Position / context | The Live carousel and VOD grid are structurally separate sections (per `navigation.md`), so live-vs-VOD distinction doesn't depend on color recognition alone |

---

## Typography

### Display / Brand Font

Not applicable. StreamKit has no public-facing brand identity to express typographically (`PRODUCT.md`: no public distribution). The app name renders in the UI font at the Heading 1 scale — no separate wordmark treatment.

---

### UI Font

- **Font:** Roboto (Android system default / Material 3 default)
- **Source:** Bundled with Android — no custom font registration needed
- **Weights used:** 400 (Regular), 500 (Medium)
- **Rationale:** `PRODUCT.md`'s "Learning over polish" principle deprioritizes typographic novelty relative to getting streaming functionality built. The system default costs zero setup and is already the Material 3 baseline this stack is built on.

### Data / Diagnostics Font

- **Font:** Roboto Mono (or system monospace)
- **Usage:** Exclusively for numeric diagnostic readouts in the Settings overlay — bitrate, buffer health, dropped frame count, resolution
- **Rationale:** these values update continuously during playback; a monospace face keeps digit width constant so the readout doesn't visually jitter as numbers change. Never used for prose or labels.

---

### Typography Scale

| Role | Size | Weight | Line Height | Usage |
|---|---|---|---|---|
| Heading 1 | 22sp | Medium | 1.3 | Catalog section headers ("Live", "Videos"), Settings screen title |
| Heading 2 | 18sp | Medium | 1.3 | Player screen video title (shown below the player in portrait) |
| Body | 15sp | Regular | 1.5 | Video descriptions, metadata |
| Body Small | 13sp | Regular | 1.5 | Secondary metadata, Settings row descriptions |
| Label | 12sp | Medium | 1.4 | Live badge, button labels, tab labels |
| Caption | 11sp | Regular | 1.4 | Timestamps, helper text |
| Data (mono) | 12sp | Regular | 1.4 | Diagnostic overlay numeric values only — see Data / Diagnostics Font above |

> Use `sp` units on Android to respect system font scaling.

---

### Playback & Diagnostic Value Formatting

This replaces the generic "Number / Amount Formatting" concern with the formatting this app actually has — there's no currency anywhere in StreamKit.

| Value type | Format |
|---|---|
| Playback timestamp | `MM:SS`, or `H:MM:SS` once past one hour — always zero-padded seconds |
| Live DVR offset | `−MM:SS` relative to the live edge, shown near the Live button when behind it |
| Bitrate | Integer `kbps`, thousands-separated at four digits (e.g. `2,500 kbps`) |
| Resolution | `WIDTHxHEIGHT`, e.g. `1920x1080` — no separator |
| Buffer health | One decimal under 10 seconds (`4.2s`), whole seconds at or above (`12s`) |
| Network type / bandwidth | `Wi-Fi · 12.4 Mbps` style — type, then a middle dot, then the estimate |
| Dropped frames | Plain integer count |

All of the above render in the Data (mono) typography role specifically because these are the values that update continuously during playback.

---

## Spacing

### Spacing Scale

| Token | Value | Usage |
|---|---|---|
| `spacing.xxs` | 2dp | Micro-gaps between inline elements |
| `spacing.xs` | 4dp | Tight internal padding |
| `spacing.sm` | 8dp | Component internal padding |
| `spacing.md` | 16dp | Standard layout padding, card content padding |
| `spacing.lg` | 24dp | Section gaps (e.g. between the Live carousel and the VOD grid) |
| `spacing.xl` | 32dp | Screen-level vertical rhythm |
| `spacing.xxl` | 48dp | Large separators |

---

## Shape / Radius

| Token | Value | Usage |
|---|---|---|
| `radius.sm` | 6dp | Live badge, chips |
| `radius.md` | 10dp | Settings rows, buttons, input-like controls |
| `radius.lg` | 16dp | VOD poster cards, Live carousel cards |
| `radius.xl` | 24dp | Settings bottom sheet, Cast device picker sheet |

---

## Elevation

Per the Tonal Elevation principle above, these are surface-tone steps, not shadow depths — there's no shadow column because shadows are not the mechanism on this theme.

| Token | Surface tone | Usage |
|---|---|---|
| `elevation.none` | `color.background.primary` | The base Catalog/Player screen background |
| `elevation.sm` | `color.surface.card` | Poster cards, the player control scrim |
| `elevation.md` | `color.surface.alt` | Settings rows, the diagnostics overlay panel |
| `elevation.lg` | `color.surface.alt` + accent-tinted border | Bottom sheets, dialogs (Cast device picker), where a visible edge in addition to tone helps separate it from the content below |

---

## Component Patterns

### Cards

Cards are the primary container for catalog content, in two variants:

**VOD poster card** (2-column grid) — `color.surface.card`, `radius.lg`, `elevation.sm`. Poster image fills the card; title sits in a label below the image (not overlaid), per `navigation.md`.

**Live carousel card** — same shape and elevation, but the title overlays the bottom-left of the poster image directly, using the functional scrim described in Visual Identity for legibility. Carries the Live badge in the top-left corner.

---

### Live Badge

**Purpose:** marks a Live carousel item as currently broadcasting.

**Anatomy:** text-only label, "Live" — no icon. Text alone is the clearest signal here; an icon would just be a second way of saying the same thing.

**Color rules:** solid `color.semantic.error` background, `color.accent.on`-style full-opacity white text. This deliberately differs from a typical low-alpha status-pill treatment — at the small size this badge renders over a busy poster image, it needs the instant legibility of a solid broadcast tally-light red, not a translucent chip.

**Shape:** `radius.sm`

**Typography:** `Label` scale, weight 500

**Padding:** `spacing.xs` vertical / `spacing.sm` horizontal

**Behavior:** Display-only — not interactive.

**Accessibility:** The accessible name is the full word "Live," announced alongside the stream title — never a bare color or icon with no text equivalent.

---

### Progress Indicators

| State | Color | Notes |
|---|---|---|
| Normal playback | `color.accent.primary` fill | Solid fill, no animation |
| Buffering / rebuffer stall | `color.semantic.warning` | Indeterminate animation while stalled |
| Playback error | `color.semantic.error` | Paired with a retry affordance, never color alone |

On the Live Player, the seekable DVR window renders as a lighter track segment within the progress bar, with the live edge marked distinctly — this is what the Live button's visibility (`BR-LIV-01`) is keyed off of.

The threshold transitions are sharp, not gradual — color changes at the exact moment the state changes (stall starts, error occurs), not a fade.

---

### Iconography

- **Icon library:** Material Symbols, consistent with the Material 3 foundation this stack is built on (`ARCHITECTURE.md`)
- **Style:** Outlined by default. Filled is reserved specifically for primary player controls — play/pause, the maximize/minimize toggle — because a filled icon stays legible at a glance against a moving video background in a way a thin outlined stroke doesn't. Outside the player overlay (nav, Settings, Catalog), stay outlined.
- **Sizes:** 24dp for player controls and primary actions; 20dp for inline/secondary icons (cast icon, Settings gear)
- **Usage rules:**
  - Icons always accompany a label or have an accessible content description
  - Icons are never used as purely decorative elements
  - The Cast icon is only ever shown when a device is actually available (`BR-CST-01`) — it's not a permanently-visible icon that becomes disabled

---

### Empty States

Catalog defines the two-level empty state pattern from `navigation.md`: dependency-not-met (no Mux assets uploaded) and no-data-yet (catalog fetch returns zero items, or a Live source fails to resolve). Anatomy: a lightweight icon, a short headline, one sentence of subtext, and — where there's a real action to take — a primary CTA.

Empty states should feel like an invitation, not an apology, and should point at the actual thing that resolves them rather than a generic "nothing here yet."

---

### Loading States

This distinction matters more here than in most apps, because StreamKit has two genuinely different kinds of "waiting":

- **List loading** (Catalog fetching from Mux): skeleton screens — poster-shaped placeholder rectangles in `color.surface.alt` with a subtle shimmer — preferred over a spinner, to avoid layout jump when content arrives.
- **Playback buffering**: a spinner/indeterminate progress state is correct here, since this is real-time feedback during an active session, not a list waiting to populate. See Progress Indicators above.

Avoid full-screen loading states on Catalog — load the Live carousel and VOD grid independently so one slow section doesn't block the other.

---

## Motion & Animation

### Motion Philosophy

Motion supports comprehension and continuity. It should never feel playful or call attention to itself — with one deliberate exception on Fire TV, below. The app should feel responsive, stable, and calm everywhere else.

### Motion Guidelines

Recommended:
- Short transitions (150–250ms) for orientation changes, the maximize/minimize toggle, and PiP enter/exit
- A subtle fade for state changes (buffering → playing, error → recovered)
- Manual swipe supported on the Live carousel; the carousel also auto-advances every 8 seconds, cyclically. A manual swipe resets the timer. Auto-advance pauses when the carousel is scrolled out of view. This is intentional: the Live section is at the top of the screen — passive browsing benefit outweighs the motion concern at this low frequency and slow interval
- **The one deliberate exception:** Fire TV's D-pad focus state uses a visible scale-up (and accent-colored focus ring) on the focused card. This isn't decorative — on a 10-foot lean-back interface with no cursor or hover state, a strong focus affordance is the primary way a viewer knows what they're about to select. This is the one place motion is allowed to be more pronounced than everywhere else in the app.

Avoid:
- Bounce or spring physics
- Parallax effects
- Long-duration transitions (> 400ms)
- Decorative motion unrelated to user intent
- Animations that block access to controls (e.g. a transition that prevents tapping play until it finishes)

---

## Accessibility

`SPEC.md`'s Accessibility section states this explicitly: "Not required for StreamKit. Personal learning project with a single known user." This document doesn't introduce accessibility requirements or NFR IDs that don't exist in `SPEC.md` — there's no `NFR-AC-*` series to point to here.

That said, a few baseline practices cost nothing extra and are followed as good hygiene rather than as a verification gate:

- **Contrast:** the palette was chosen with reasonable contrast in mind (off-white text on near-black, not pure white-on-black or low-contrast gray-on-gray) — this is a side effect of choosing a calm, readable palette, not a formal WCAG audit target.
- **Touch targets:** Material's standard 48dp minimum is a reasonable default to follow for buttons and controls simply because it's the platform default, not because it's a tracked requirement.
- **Color independence:** see the Color Independence Rule above — followed because it's good design regardless of formal accessibility scope.

If a future phase changes StreamKit's single-user assumption, this section is where formal WCAG targets and `SPEC.md` NFRs would need to be added together, in lockstep.

---

## Platform Adaptation

### Android (`app`)

Android uses **Material 3** as the foundational design system (`ARCHITECTURE.md`). StreamKit customizes Material 3's color scheme, typography, shape, and elevation tokens while preserving Android-native interaction expectations (ripple feedback, system back gesture, standard touch target sizing).

| Material 3 Role | StreamKit Token |
|---|---|
| `primary` | `color.accent.primary` |
| `onPrimary` | `color.accent.on` |
| `background` | `color.background.primary` |
| `onBackground` | `color.text.primary` |
| `surface` | `color.surface.card` |
| `onSurface` | `color.text.primary` |
| `surfaceVariant` | `color.surface.alt` |
| `onSurfaceVariant` | `color.text.secondary` |
| `outline` | `color.border.default` |
| `error` | `color.semantic.error` |
| `onError` | `color.accent.on` |

`secondary`, `tertiary`, and their `on*`/`*Container` counterparts are intentionally left at Material 3 defaults — StreamKit's palette defines a single accent (Signal-Accent principle, above) and has no secondary or tertiary hue. No component in this design system references these roles.

### Typography Mapping

`StreamKitTypography`'s roles (defined in Typography, above) map onto Material 3's `Typography` object so stock Material composables that read `MaterialTheme.typography.*` internally (default `Text()` style, `Button`, `TopAppBar` title, `Tab` labels) stay visually consistent without needing to reference `StreamKitTypography` explicitly everywhere.

| StreamKit Role | M3 Typography Role |
|---|---|
| Heading 1 | `titleLarge` |
| Heading 2 | `titleMedium` |
| Body | `bodyLarge` |
| Body Small | `bodyMedium` |
| Label | `labelLarge` |
| Caption | `labelSmall` |
| Data (mono) | *(no M3 mapping — diagnostics-only, referenced directly via `StreamKitTypography`, never a stock component default)* |

`StreamKitTypography` remains the source of truth, exposed via its own `CompositionLocal`; the M3 `Typography` object above exists solely so unstyled Material internals fall back to the correct scale rather than generic Material defaults.

---

### Fire TV (`tv`)

Fire TV uses **Compose for TV** (`androidx.tv`) with the same color and typography tokens as `app` — there is no separate TV palette. What changes is interaction and density, not identity:

- D-pad focus replaces touch as the primary interaction signal — see the Motion exception above
- Text and touch/focus target sizes increase for 10-foot viewing distance
- The accent-colored focus ring is the TV equivalent of Android's ripple/press state
- No PiP, no maximize/minimize toggle (Fire TV is always presented full-screen lean-back), no Cast icon (Fire TV is a cast *target*, not a client) — these aren't omissions, they're correct per `navigation.md`'s Fire TV notes

---

## Design Tokens

The token naming strategy below is the canonical reference for implementation. Platform-specific implementations map to these token names.

### Color Tokens

```
color.background.primary
color.surface.card
color.surface.alt
color.border.default
color.text.primary
color.text.secondary
color.accent.primary
color.accent.on
color.semantic.success   (alias of color.accent.primary)
color.semantic.error
color.semantic.warning
color.semantic.info      (alias of color.accent.primary)
```

### Spacing Tokens

```
spacing.xxs   — 2dp
spacing.xs    — 4dp
spacing.sm    — 8dp
spacing.md    — 16dp
spacing.lg    — 24dp
spacing.xl    — 32dp
spacing.xxl   — 48dp
```

### Radius Tokens

```
radius.sm     — 6dp    (Live badge, chips)
radius.md     — 10dp   (Settings rows, buttons)
radius.lg     — 16dp   (poster cards)
radius.xl     — 24dp   (bottom sheets, dialogs)
```

### Elevation Tokens (tonal surface steps, not shadow depth)

```
elevation.none   — color.background.primary   (base screen)
elevation.sm     — color.surface.card          (poster cards, control scrim)
elevation.md     — color.surface.alt           (Settings rows, diagnostics panel)
elevation.lg     — color.surface.alt + border  (sheets, dialogs)
```

---

## Future Considerations

The design system is expected to evolve alongside the product.

Potential future additions:

- Chart visualization standards and a multi-series color palette for the Phase 6 QoE dashboard
- A dedicated component specification document, once `specs/features/*/design.md` files start getting written
- Cast UI states for Phase 2 (connecting, connected, casting-in-progress visual treatment)
- DRM error-state visual treatment for Phase 5 (license failure messaging, per `BR-DRM-03`)
- Fire TV D-pad focus-order and remote-control iconography refinement once `tv` implementation starts
- Formal accessibility targets, if StreamKit's single-user assumption ever changes (see Accessibility above)

Future additions must remain consistent with the core philosophy defined in this document.

---

## Guiding Principle

When in doubt, ask whether a design decision makes the content easier to watch and the diagnostics easier to read at a glance — not whether it makes the app look more impressive. The chrome's job is to disappear; the signal's job is to be unmistakable when it matters.

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-26 | Danielle Mariani | Initial draft — dark-only color system, typography, spacing/shape/elevation tokens, component patterns (cards, Live badge, progress indicators), motion, lightweight accessibility note, and Android/Fire TV platform adaptation. Built on the palette approved in chat and the screen behaviors locked in `navigation.md` |
| 0.1.1 | 2026-07-04 | Danielle Mariani | Updated Motion Guidelines: Live carousel now auto-advances every 8s (cyclic); manual swipe resets the timer; pauses when out of view. Replaces prior "manual swipe only" guidance, per decision in `specs/features/catalog/design.md` |
| 0.1.2 | 2026-07-07 | Danielle Mariani | Gaps found during TSK-CAT-03 implementation: added `onBackground`/`onSurface`/`onSurfaceVariant` to the Material 3 Role table (mapped to `color.text.primary`/`color.text.secondary`), with a note that `secondary`/`tertiary` are intentionally left at Material defaults since StreamKit has no secondary/tertiary accent; added a new Typography Mapping table (StreamKit roles → M3 `Typography` roles) so stock Material components fall back to the correct scale |