# LastHavenPlus v2.4.0 — A Frosty New Look

This update gives the launcher a fresh icy-blue look, improves accessibility and reliability, and introduces the first phase of the native Addon Manager.

## New features

- **Native Addon Manager (Phase A):** Import and export addon ZIP files, keep them in a private local library, and inspect addon details and dependencies.
- **Accessibility improvements:** The game menu has clearer screen-reader structure, a keyboard/controller focus entry point, and transitions that respect Android's reduced-motion setting.
- **A refreshed visual theme:** Launcher screens now share an icy-blue accent palette, more consistent typography, shapes and spacing.

**Addon Manager limitation:** Addons can be stored, inspected and organized, but they are not activated inside the game. Runtime loading and game integration are not part of this update.

## Bug fixes and reliability

- Update checks no longer run automatically when the launcher starts. You choose when to check.
- Addon-library changes are staged and can be rolled back safely if an operation is interrupted.

## Small changes

- New icy-blue pixel-art castle launcher icon.
- More consistent buttons, cards, spacing and touch targets.
- Subtle menu transitions and haptic feedback for primary actions.

## Compatibility

- Android 10 (API 29) or newer.
- ARM64 devices only (arm64-v8a).
- Mozilla GeckoView 160.
- Existing application ID and release signing identity are preserved.

GeckoView uses its own browser profile. You may need to sign in to the game again after upgrading.

**Device-testing note:** Automated build checks do not replace testing on a physical ARM64 device. Gameplay, TalkBack behavior, launcher-icon masking and the Addon Manager document picker have not been verified on physical hardware for this release.
