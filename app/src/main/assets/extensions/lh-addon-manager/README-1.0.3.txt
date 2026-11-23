Lasthavens Addon Manager 1.0.3

Data safety
- Add-on installs and updates now write files to a fresh IndexedDB storage generation before switching the library metadata pointer.
- If writing files or saving metadata fails, the previous installed add-on generation remains available.
- Existing add-ons keep their current storage keys and continue to load without a data migration.
- Old storage generations are removed only after the new library metadata is committed; interrupted cleanup is retried during manager startup.
- Existing add-on folder names, versions, enabled state, library metadata, manager settings, and Gecko profile data are preserved.
- Archive imports, restores, and remote downloads enforce archive/file/expanded-size limits against actual streamed bytes.

Fixes
- Numbered TOC dependencies are ordered numerically (Dep1, Dep2, Dep10).

Validation
- Added regression coverage for numeric dependency ordering and storage-generation metadata replacement.
- Full CI and physical-device verification are pending.

Backup scope
- ZIP backups contain the files of managed add-ons only.
- They do not contain manager settings, enabled/disabled flags, game SavedVariables, login/session data, or the shared Gecko profile.
- Restored add-ons are disabled by default when newly added. When restoring over an existing add-on, its current enabled/disabled state is preserved by the library merge logic.
- A ZIP restore is an add-on file import, not a full application/profile restore.


## Game site storage estimate (Addon Manager 1.0.3)

The Addon Manager displays the browser StorageManager estimate for the Lasthavens game origin when the game page is open. It reports origin-scoped website storage usage (and quota when available), separately from app-wide shared GeckoView profile usage. The estimate refreshes while the game page remains open. It is not a measure of lifetime network downloads, and browser estimates may be approximate.
