# Release changelog

## v1.0.0 — stable release notes

These notes describe the original v1.0.0 release on the stable `main` branch. They are historical release notes; beta has since migrated from Cefrium to GeckoView.

### Launcher and game
- Launch the existing LastHaven Wrath of the Lich King browser game.
- Preserve the existing application ID and release signing identity for in-place Android updates.
- Package ARM64 (`arm64-v8a`) only.

### Settings and diagnostics
- Show installed APK size, browser/profile data when available and app cache usage.
- Show app-wide received and sent network bytes. These counters include game and updater traffic; they are not game-only measurements.
- Provide local diagnostic events, a log viewer and explicit user-initiated sharing.

### Updates
- Check stable GitHub Releases and let the user choose whether to download an update.
- Offer a differential update when the installed base APK matches, with a full APK fallback.
- Validate downloaded/reconstructed APK checksums, package identity, version and established signing certificate before handing installation to Android.
- Never download or install updates automatically.

### Privacy and limitations
- Keep diagnostics and usage measurements on-device; no analytics or automatic log uploads.
- Redact common credentials and session-related values from diagnostic messages.
- Physical-device gameplay, GPU, audio, persistence and in-place update verification are separate from CI build success.

## Current development

For the current GeckoView implementation, beta verification and outstanding work, see [README.md](README.md) and [STATUS.md](STATUS.md). This changelog does not imply that beta is released or approved for promotion.
