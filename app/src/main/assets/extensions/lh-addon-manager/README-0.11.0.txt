Lasthavens Addon Manager 0.11.0

New in 0.11.0
- Live Game Scan observes addon file requests made by the running Lasthavens game.
- The manager distinguishes addons prepared for injection from addons whose files the game has actually requested.
- The game-page status badge and manager diagnostics report observed addon requests and injection preparation failures.
- Update All checks catalog entries for clearly newer compatible releases.
- Same-folder addons from multiple sources are grouped into one Browse entry; choose a source/version from the selector.
- The newest catalog version is preferred. The importer refuses same-version, older, or unverifiable replacements, preventing accidental downgrades.
- Back up all managed addon files into one ZIP and restore from that ZIP.
- Dashboard counts, installed/not-installed/update filters, operation progress, and a copyable error log.

Important limitations
- The scan detects addon files requested by the game through the extension's injection layer. It does not prove that the addon's Lua code initialized successfully.
- Load-on-demand addons may not be requested until the game or another addon needs them.
- The browser cannot silently inspect a separate local WoW installation.
- Backup ZIPs include addon files only. They do not include SavedVariables, game settings, or manager enabled/disabled flags.
- Update detection depends on TOC version metadata. Addons without comparable versions are not automatically updated.
- The XPI is unsigned. Normal Firefox release installation requires Mozilla signing; temporary installation may be used for testing.

Addon screenshots
-----------------
The Browse tab now displays a preview when a catalog entry provides a screenshot URL,
including repository scans that find an image inside the add-on folder. Screenshot
previews are lazy-loaded; selecting one opens the image in a browser tab. Entries with
no usable screenshot continue to display normally.
