Lasthavens Addon Manager 1.0.2

Data safety
- Add-on installs and updates now write files to a fresh IndexedDB storage generation before switching the library metadata pointer.
- If writing files or saving metadata fails, the previous installed add-on generation remains available.
- Existing add-ons keep their current storage keys and continue to load without a data migration.
- Old storage generations are removed only after the new library metadata is committed; interrupted cleanup is retried during manager startup.
- Existing add-on folder names, versions, enabled state, library metadata, manager settings, and Gecko profile data are preserved.

Fixes
- Numbered TOC dependencies are ordered numerically (Dep1, Dep2, Dep10).

Validation
- Added regression coverage for numeric dependency ordering and storage-generation metadata replacement.
- Full CI and physical-device verification are pending.
