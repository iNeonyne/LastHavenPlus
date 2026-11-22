Lasthavens Addon Manager 1.0.0

Version baseline
- The Android project now tracks the bundled Addon Manager as version 1.0.0.
- This is a project version reset/baseline, not a claim that the manager was rewritten.
- The 1.0.0 baseline retains the existing manager functionality documented in README-0.11.0.txt.
- No add-on files, extension storage, Gecko profile data, or manager settings are intentionally changed by this version metadata update.

Versioning policy
- The authoritative version is the "version" field in manifest.json.
- Use semantic versioning for future manager changes: patch for compatible fixes, minor for backward-compatible features, and major for breaking changes.
- Whenever manager functionality changes, update manifest.json, this file (or add a version-specific release note), the root README version snapshot, and PROJECT_OVERVIEW.md in the same change.
- Do not change extension storage formats or migration behavior without a tested backup, migration, and rollback plan.

Baseline capabilities carried forward
- Live Game Scan observes addon file requests made by the running Lasthavens game.
- The manager distinguishes addons prepared for injection from addons whose files the game has actually requested.
- The game-page status badge and manager diagnostics report observed addon requests and injection preparation failures.
- Update All checks catalog entries for clearly newer compatible releases.
- Same-folder addons from multiple sources are grouped into one Browse entry; choose a source/version from the selector.
- The newest catalog version is preferred. The importer refuses same-version, older, or unverifiable replacements, preventing accidental downgrades.
- Back up all managed addon files into one ZIP and restore from that ZIP.
- Dashboard counts, installed/not-installed/update filters, operation progress, and a copyable error log.
- Browse tab screenshot previews where catalog entries provide a usable screenshot URL.

Important limitations
- Live Game Scan detects addon files requested through the extension's injection layer; it does not prove Lua initialization succeeded.
- Load-on-demand addons may not be requested until the game or another addon needs them.
- Backups contain managed addon files only; they do not include SavedVariables, game settings, or manager enabled/disabled flags.
- Update detection depends on comparable TOC version metadata.
- The XPI is unsigned; normal Firefox release installation requires Mozilla signing.
