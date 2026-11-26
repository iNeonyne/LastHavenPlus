# Launcher design system — issue #107 (v2.4.0)

Launcher-owned Compose surfaces only. Gameplay is fullscreen and unobstructed; this system does not touch the game, GeckoView, or any app behavior.

## Palette

Dark-fantasy base retained from the v2.3.0 unification; the accent family moved from gold to the icy blue that matches the #133 launcher-icon direction (deep navy + icy-blue moon/glow, no gold portal).

### Dark scheme (shipped render target)

| Token | Value | Used for |
|---|---|---|
| `ink` | `#080D18` | Page background, status/navigation bar color |
| `gradientTop` / `gradientBottom` | `#17263A` / `#05080F` | Home vertical gradient |
| `panel` | `#121D2D` | Cards, menu surface |
| `panelBright` | `#26364B` | Attention cards (update badge) |
| `panelOverlay` | `#F2121D2D` | Back-overlay menu panel |
| `logSurface` | `#080B12` | Diagnostics log body |
| `scrim` | `#58000000` (34.5% alpha) | Behind the Back overlay |
| `frost` | `#9BDCF5` | Accent: section titles, eyebrows, labels, progress, primary buttons |
| `frostOnColor` | `#0A1621` | Text on `frost` fills |
| `textPrimary` | `#FFFFFF` | Headings and emphasized values |
| `textBody` | `#D7E0EC` | Log body text |
| `textSecondary` | `#AAB7C9` | Supporting copy |
| `error` | `#ED8796` | M3 error role (unused by screens so far) |

### Light scheme (non-persisted token variant, preview-only)

Supplied as `DesignTokens.colorScheme(darkTheme = false)` and `homeGradient(darkTheme = false)`. **The app currently renders the dark scheme everywhere; no preference control or persistence exists (that belongs to #6).** The screens still consume the fixed dark token constants; full light rendering of real screens is a follow-up adoption step, so the light values are demonstrated by previews, not by the running app.

| Token | Value | Pairing |
|---|---|---|
| background | `#F4F8FC` | onBackground `#10202F` (15.5:1) |
| surface | `#E7F0F7` | onSurface `#10202F` |
| surfaceVariant | `#D8E7F2` | onSurfaceVariant `#52687C` (5.0–5.4:1) |
| primary | `frostDeep #0A5E84` | onPrimary `#FFFFFF` (7.1:1) |
| home gradient | `#E2EEF7 → #F4F8FC → #FFFFFF` | frost accents swap to `frostDeep` |
| error | `#B3261E` | M3 default |

`primaryButtonColors()` resolves from `MaterialTheme.colorScheme`, so the same component token serves both variants; under the app's fixed dark scheme it produces exactly the previous frost/frostOnColor pairing.

## Contrast guarantees (unit-tested in `DesignTokensTest`)

WCAG relative-luminance ratios computed by the shared `contrastRatio()` helper:

| Pair | Ratio | Requirement |
|---|---:|---|
| `textPrimary` on `panel` | 16.9 | ≥ 4.5 |
| `textSecondary` on `panel` | 8.3 | ≥ 4.5 |
| `textSecondary` on `ink` | 9.6 | ≥ 4.5 |
| `textBody` on `logSurface` | 14.8 | ≥ 4.5 |
| `frost` on `panel` | 11.3 | ≥ 8.0 |
| `frost` on `ink` | 12.9 | ≥ 8.0 |
| `frost` on `panelBright` | 8.2 | ≥ 7.0 |
| `frostOnColor` on `frost` | 12.1 | ≥ 8.0 |
| `frostDeep` on light background `#F4F8FC` | 6.7 | ≥ 4.5 |
| `frostDeep` on light surface `#E7F0F7` | 6.2 | ≥ 4.5 |
| `#FFFFFF` on `frostDeep` | 7.1 | ≥ 4.5 |

The utility pairs are unit-tested in `DesignTokensTest`; the light-scheme rows too. Section headings added in this pass use the `titleSmall` role (frost on panel, 11.3:1 at any font scale); `labelSmall` remains the page-eyebrow role only (`LASTHAVENPLUS`, `GAME MENU`).

## Tokens beyond color

- **Spacing:** `spaceSmall` 8, `spaceMedium` 12, `spaceLarge` 16, `spaceSection` 18, `spaceScreen` 28; `minTouchTarget` 48 dp.
- **Shapes (M3 role map):** extraSmall 8, small = `cornerButton` 14, medium = `cornerCard` 16, large = `cornerDialog` 24, extraLarge = `cornerBadge` 32. Cards use `MaterialTheme.shapes.medium`; the primary dialog/overlay uses `cornerDialog`.
- **Typography:** `DesignTokens.typography()` derives from the M3 defaults — `headlineSmall`/`titleLarge` SemiBold, `labelSmall` SemiBold with 1.5 sp tracking. `labelSmall` is the page-eyebrow role only (`LASTHAVENPLUS`, `GAME MENU`); section headings use the `titleSmall` role.
- **Component tokens:** `DesignTokens.primaryButtonColors()` is the single source for primary-action button colors; every screen uses it.

## Application surface

- **Back overlay** (`GameMenuScreen`): `panelOverlay` panel over `scrim`, `labelSmall` frost eyebrow, primary button on Reload.
- **Game states** (`GameScreen`): frost progress and Retry/Restore primary buttons.
- **Home** (`HomeScreen`): gradient, brand block (`PLUS` in frost), card and badge shapes unified to `shapes.medium`, haptic on Play only.
- **Settings / Diagnostics / Update** (`SettingsDialogs`): shared `SettingsPageFrame` (ink page, eyebrow header), shared `PageSection` (panel card, frost `titleSmall` heading).

## Verification checklist

Repeatable, tooling-based checks shipped in this change:

- **Contrast (automated):** `DesignTokensTest` asserts palette anchors and every ratio in the table above, including the light-scheme rows.
- **Theme variants (Compose previews):** `DesignPreviews.kt` renders the same token showcase under `Theme dark`, `Theme light`, and `System dark/light` (via `uiMode` + `isSystemInDarkTheme()`); the system previews are 360×640 dp.
- **Breakpoints (Compose previews):** `GameMenuNarrowPreview` (360×640 dp, below `WIDE_HOME_MIN_WIDTH_DP`) and `GameMenuWidePreview` (1040×620 dp, above it, matching `LARGE_SETTINGS_MIN_WIDTH_DP`-class windows) exercise the shared overlay composition at both layout regimes. Home/settings page behavior is governed by the pre-existing `LauncherLayoutPolicy` (700/900/420 dp breakpoints), which is unchanged here and covered by its own unit tests on beta; this PR does not newly validate those behaviors, only inherits them.
- **Font scaling:** headings use M3 scale roles (`titleSmall`/`headlineSmall`), so enlarged font-scale rendering must still be checked in tooling or on device; token colors meet ≥8:1 contrast regardless of size.

## Verification and limits

- `DesignTokensTest` runs in the exact-head unsigned PR validation along with `lintDebug spotlessCheck assembleRelease testDebugUnitTest`.
- Compose previews are tooling artifacts; they do not prove physical ARM64 rendering, haptic behavior, or real-mask icon legibility, and no physical-device testing has been performed. Light-scheme rendering of the real screens is not implemented (screens consume fixed dark token constants); the light variant is token+preview only, with persistence/control deferred to #6.
- Launcher icon artwork is owned by #133 (merged via PR #135; issue open with `agent:owner` for mask/physical signoff) and is out of scope here.
