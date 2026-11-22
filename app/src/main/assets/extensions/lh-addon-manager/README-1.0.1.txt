Lasthavens Addon Manager 1.0.1

Fixes
- Permission checks now fail closed when the browser permissions API is unavailable or throws an error.
- The Browse view no longer treats an unsuccessful permission check as permission granted.
- The game-site permission indicator remains visible when its permission check cannot be completed.

Compatibility and data
- Compatible patch update to Addon Manager 1.0.0.
- No add-on files, IndexedDB records, library metadata, manager settings, or Gecko profile data are migrated, reset, or rewritten by this fix.

Validation
- Added CI regression coverage for permission API success, denial, and error cases.
- Added regression coverage for TOC parsing, dependency ordering, library merge behavior, archive round-trips, traversal paths, and duplicate ZIP entries.
