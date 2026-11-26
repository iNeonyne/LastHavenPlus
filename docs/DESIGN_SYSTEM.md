# Launcher design system — issue #107 (v2.4.0)

Launcher-owned Compose surfaces only. Gameplay is fullscreen and unobstructed; this system does not touch the game, GeckoView, or any app behavior.

## Palette

Dark-fantasy base retained from the v2.3.0 unification; the accent family moved from gold to the icy blue that matches the #133 launcher-icon direction (deep navy + icy-blue moon/glow, no gold portal).

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

## Tokens beyond color

- **Spacing:** `spaceSmall` 8, `spaceMedium` 12, `spaceLarge` 16, `spaceSection` 18, `spaceScreen` 28; `minTouchTarget` 48 dp.
- **Shapes (M3 role map):** extraSmall 8, small = `cornerButton` 14, medium = `cornerCard` 16, large = `cornerDialog` 24, extraLarge = `cornerBadge` 32. Cards use `MaterialTheme.shapes.medium`; the primary dialog/overlay uses `cornerDialog`.
- **Typography:** `DesignTokens.typography()` derives from the M3 defaults — `headlineSmall`/`titleLarge` SemiBold, `labelSmall` SemiBold with 1.5 sp tracking. `labelSmall` is the eyebrow/section-title role (for example `GAME MENU`, `LASTHAVENPLUS`, section titles).
- **Component tokens:** `DesignTokens.primaryButtonColors()` is the single source for primary-action button colors; every screen uses it.

## Application surface

- **Back overlay** (`GameMenuScreen`): `panelOverlay` panel over `scrim`, `labelSmall` frost eyebrow, primary button on Reload.
- **Game states** (`GameScreen`): frost progress and Retry/Restore primary buttons.
- **Home** (`HomeScreen`): gradient, brand block (`PLUS` in frost), card and badge shapes unified to `shapes.medium`, haptic on Play only.
- **Settings / Diagnostics / Update** (`SettingsDialogs`): shared `SettingsPageFrame` (ink page, eyebrow header), shared `PageSection` (panel card, frost `labelSmall` title).

## Verification and limits

- `DesignTokensTest` asserts palette anchors and the contrast table above; it runs in the exact-head unsigned PR validation along with `lintDebug spotlessCheck assembleRelease testDebugUnitTest`.
- Lint output, previews and CI are not proof of physical ARM64 rendering, haptic behavior, or real-mask icon legibility; those need device verification. Launcher icon artwork itself is owned by #133 and is out of scope here.
