Lasthavens Addon Manager 1.0.5

Changes
- Added explicit optional host-permission requests for supported GitHub and GitLab add-on download sources.
- Improved error reporting for ZIP imports and Android file-picker failures so failed operations are visible instead of silently failing.
- Kept add-on installation on the existing staged storage path so failed installs do not replace the previously committed add-on library.

Validation
- Automated Addon Manager logic and archive-handling tests are run in CI.
- Full CI validation for this release is pending.

Backup scope
- ZIP backups contain the files of managed add-ons only.
- They do not contain manager settings, enabled/disabled flags, game SavedVariables, login/session data, or the shared Gecko profile.
- Restored add-ons are disabled by default when newly added. When restoring over an existing add-on, its current enabled/disabled state is preserved by the library merge logic.
- A ZIP restore is an add-on file import, not a full application/profile restore.

Game site storage estimate
- The Addon Manager displays the browser StorageManager estimate for the Lasthavens game origin when the game page is open.
- It reports origin-scoped website storage usage (and quota when available), separately from app-wide shared GeckoView profile usage.
- The estimate refreshes while the game page remains open. It is not a measure of lifetime network downloads, and browser estimates may be approximate.
