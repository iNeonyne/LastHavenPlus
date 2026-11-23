# GeckoView runtime and profile ownership

## Runtime creation

`App.onCreate()` creates the application's single `GeckoRuntime` with
`GeckoRuntime.create(this, ...)`. Runtime creation is skipped in GeckoView child
processes by `isMainProcess()`. There is no second runtime factory or explicit
profile-directory override in the application code.

The application therefore relies on GeckoView's default profile associated with
that one runtime. The app does not manually copy, export, or reset that profile
when opening either screen.

## Session ownership

- **Play:** `App.gameSession()` owns one long-lived `GeckoSession`. It opens
  that session with `open(runtime)` and retains it when `PlayActivity` is
  destroyed during ordinary navigation away from the game. Returning to Play
  reattaches the existing session when it is still open.
- **Addon Manager:** `ManagerActivity.openManager()` creates a separate,
  short-lived `GeckoSession` and opens it with `open(app.runtime)`. It
  therefore uses the same runtime/profile as Play, while keeping its own
  navigation/session state.
- **Bundled extension:** `App.installBuiltInExtension()` installs the
  Addon Manager extension through that same runtime's
  `webExtensionController`. The manager page is loaded from the extension's
  `metaData.baseUrl`; add-on storage managed by the extension is consequently
  available to the game through the shared profile.

## Lifecycle and data implications

Destroying `ManagerActivity` detaches its delegates and closes only its
manager session. Destroying `PlayActivity` detaches its activity-bound
delegates and releases the view's session attachment, but does not close the
application-owned game session. A Gecko content-process crash/kill discards
the game session and recreates the Play screen, or recreates the Manager
screen; it does not intentionally clear the runtime profile.

The manifest sets `android:allowBackup="false"`. This disables Android
Auto Backup for the app; it does **not** mean normal same-package,
same-signing-certificate app updates should erase private app data. The app
does not contain an explicit profile migration or reset in the reviewed paths.

## Verification boundary

The statements above describe the checked application code and intended
ownership model. They do not replace a physical-device test. Verify that
managed add-ons remain visible after navigating between Manager and Play,
after process death/relaunch, and after an in-place APK update signed with
the permanent release key. Do not treat an app uninstall/reinstall as an
update test: uninstalling can remove private app data.
