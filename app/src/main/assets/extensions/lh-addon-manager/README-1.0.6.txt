Addon Manager 1.0.6

- Adds a versioned, checksummed migration export containing add-on files, library metadata, enabled state, dependencies, and manager preferences.
- Validates file paths, storage identities, file counts, per-file sizes, total expanded size, and compressed archive size before producing the export.
- Export is read-only and does not alter the existing Gecko-backed library.
- The native importer is a separate migration phase; keep the generated package until the native manager confirms a verified import.
- Existing Gecko profile data and login/session state are not deleted or automatically transferred.
