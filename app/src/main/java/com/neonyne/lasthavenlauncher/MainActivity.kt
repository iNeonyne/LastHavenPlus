package com.neonyne.lasthavenlauncher

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Locale
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession

private const val DEFAULT_GAME_URL = "https://lasthavens.com/play"
private const val PREFERENCES_NAME = "launcher_settings"
private const val KEY_GAME_URL = "game_url"
private const val TAG = "LastHavenLauncher"
private const val KEY_PENDING_UPDATE_URI = "pending_update_uri"

enum class Screen {
  Home,
  Game,
  Settings,
  Diagnostics,
  Update,
}

class MainActivity : ComponentActivity() {
  private var pendingUpdateUri: Uri? = null
  private var immersiveActive = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
    pendingUpdateUri = preferences.getString(KEY_PENDING_UPDATE_URI, null)?.let(Uri::parse)
    Diagnostics.info(this, "Lifecycle", "Activity created")
    WindowCompat.setDecorFitsSystemWindows(window, true)
    setContent {
      LastHavenApp(
        preferences = preferences,
        onExit = { finish() },
        onGameplayUiChanged = ::setGameplayUi,
        onCheckUpdates = { callback -> AppUpdater.checkForUpdate(this, callback) },
        onDownloadUpdate = { update, preferDelta, callback ->
          AppUpdater.downloadUpdate(this, update, preferDelta, callback)
        },
        onInstallUpdate = ::installUpdate,
      )
    }
  }

  private fun setGameplayUi(gameplay: Boolean, immersive: Boolean) {
    immersiveActive = immersive
    WindowCompat.setDecorFitsSystemWindows(window, !gameplay)
    requestedOrientation =
      if (gameplay) {
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
      } else {
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
      }
    if (gameplay) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    if (immersive) {
      WindowInsetsControllerCompat(window, window.decorView).apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    } else {
      window.statusBarColor = android.graphics.Color.rgb(8, 13, 24)
      window.navigationBarColor = android.graphics.Color.rgb(8, 13, 24)
      WindowInsetsControllerCompat(window, window.decorView).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = false
        show(WindowInsetsCompat.Type.systemBars())
      }
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus && immersiveActive) setGameplayUi(true, true)
  }

  private fun installUpdate(uri: Uri) {
    val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
    if (!packageManager.canRequestPackageInstalls()) {
      pendingUpdateUri = uri
      check(preferences.edit().putString(KEY_PENDING_UPDATE_URI, uri.toString()).commit()) {
        "Could not save the pending update installer request"
      }
      startActivity(
        Intent(
          Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
          Uri.parse("package:$packageName"),
        )
      )
      return
    }

    startActivity(
      Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
    )
    pendingUpdateUri = null
    if (!preferences.edit().remove(KEY_PENDING_UPDATE_URI).commit()) {
      Diagnostics.warn(this, "Updater", "Could not clear pending installer recovery state")
    }
  }

  override fun onResume() {
    super.onResume()
    Diagnostics.info(this, "Lifecycle", "Activity resumed")
    if (pendingUpdateUri == null) {
      pendingUpdateUri =
        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
          .getString(KEY_PENDING_UPDATE_URI, null)
          ?.let(Uri::parse)
    }
    val uri = pendingUpdateUri
    if (uri != null && packageManager.canRequestPackageInstalls()) {
      installUpdate(uri)
    }
  }
}

@Composable
private fun LastHavenApp(
  preferences: SharedPreferences,
  onExit: () -> Unit,
  onGameplayUiChanged: (Boolean, Boolean) -> Unit,
  onCheckUpdates: ((UpdateCheckResult) -> Unit) -> Unit,
  onDownloadUpdate: (AppUpdateInfo, Boolean, (Result<Uri>) -> Unit) -> Unit,
  onInstallUpdate: (Uri) -> Unit,
) {
  val context = LocalContext.current
  val usageMeter = remember { AppUsageMeter(context) }
  val gameUrl = remember {
    preferences.getString(KEY_GAME_URL, DEFAULT_GAME_URL) ?: DEFAULT_GAME_URL
  }

  var currentScreen by remember { mutableStateOf(Screen.Game) }
  var menuVisible by remember { mutableStateOf(false) }
  var settingsReturnScreen by remember { mutableStateOf(Screen.Game) }
  var diagnosticsReturnScreen by remember { mutableStateOf(Screen.Settings) }
  var updateReturnScreen by remember { mutableStateOf(Screen.Game) }
  var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
  var updateMessage by remember { mutableStateOf<String?>(null) }
  var isDownloadingUpdate by remember { mutableStateOf(false) }
  var downloadingDelta by remember { mutableStateOf(false) }
  var isCheckingUpdate by remember { mutableStateOf(false) }
  var runtime by remember { mutableStateOf<org.mozilla.geckoview.GeckoRuntime?>(null) }
  var session by remember { mutableStateOf<GeckoSession?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  var pageLoadFailed by remember { mutableStateOf(false) }
  var sessionCrashed by remember { mutableStateOf(false) }
  var hasLoadedGame by remember { mutableStateOf(false) }
  var hasCompletedLoad by remember { mutableStateOf(false) }

  // Keep the GeckoSession alive while its view is detached for launcher destinations.
  DisposableEffect(session) {
    val sessionToClose = session
    onDispose { sessionToClose?.close() }
  }

  val attachGameSession: (GeckoSession) -> Unit = { gameSession ->
    setupGeckoDelegates(
      gameSession,
      context,
      onLoadState = { loading, failed ->
        isLoading = loading
        if (!failed) {
          pageLoadFailed = false
          sessionCrashed = false
        }
      },
      onPageLoaded = { hasCompletedLoad = true },
      onSessionCrashed = {
        isLoading = false
        pageLoadFailed = false
        sessionCrashed = true
      },
    )
  }

  val reloadGame: () -> Unit = {
    val current = session
    if (current?.isOpen() == true) {
      pageLoadFailed = false
      sessionCrashed = false
      current.reload()
    } else if (current != null) {
      val currentRuntime = runtime ?: GeckoRuntimeProvider.get(context).also { runtime = it }
      current.open(currentRuntime)
      current.loadUri(gameUrl)
      pageLoadFailed = false
      sessionCrashed = false
    }
  }

  val startGame: () -> Unit = {
    if (session == null) {
      val gameRuntime = runtime ?: GeckoRuntimeProvider.get(context).also { runtime = it }
      val gameSession = GeckoSession()
      attachGameSession(gameSession)
      gameSession.open(gameRuntime)
      session = gameSession
      pageLoadFailed = false
      gameSession.loadUri(gameUrl)
      hasLoadedGame = true
    } else if (session?.isOpen() != true) {
      val gameRuntime = runtime ?: GeckoRuntimeProvider.get(context).also { runtime = it }
      session?.open(gameRuntime)
      if (!hasLoadedGame) {
        session?.loadUri(gameUrl)
        hasLoadedGame = true
      }
    }
    menuVisible = false
    currentScreen = Screen.Game
  }

  // A crashed content process cannot be reloaded in place; recreate the session while
  // keeping the runtime (and therefore the game profile) intact.
  val restoreGame: () -> Unit = {
    if (session != null) {
      session = null
      hasLoadedGame = false
      hasCompletedLoad = false
      pageLoadFailed = false
      isLoading = false
      sessionCrashed = false
      startGame()
    }
  }

  val checkForUpdates: (Boolean) -> Unit = { showResult ->
    isCheckingUpdate = true
    updateMessage = null
    onCheckUpdates { result ->
      isCheckingUpdate = false
      when (result) {
        is UpdateCheckResult.Available -> {
          availableUpdate = result.update
          updateMessage = null
        }
        is UpdateCheckResult.Failed -> {
          updateMessage = result.message
        }
        UpdateCheckResult.NoUpdate -> {
          availableUpdate = null
          updateMessage = if (showResult) "You are using the latest version." else null
        }
      }
    }
  }

  val openUpdate: (Screen) -> Unit = { returnTo ->
    updateReturnScreen = returnTo
    currentScreen = Screen.Update
  }

  val openSettings: (Screen) -> Unit = { returnTo ->
    settingsReturnScreen = returnTo
    val target = LauncherNavigationPolicy.openSettings()
    currentScreen = target.screen
    menuVisible = target.menuVisible
  }

  val gameplayActive = LauncherNavigationPolicy.isGameplay(currentScreen)
  val immersiveGameplay = LauncherNavigationPolicy.isImmersiveGameplay(currentScreen, menuVisible)
  SideEffect { onGameplayUiChanged(gameplayActive, immersiveGameplay) }
  DisposableEffect(Unit) {
    onDispose { onGameplayUiChanged(false, false) }
  }

  LaunchedEffect(Unit) {
    // Start/resume the game immediately; the metadata check never downloads or installs.
    startGame()
    checkForUpdates(false)
  }

  BackHandler(enabled = currentScreen != Screen.Home || menuVisible) {
    val target =
      LauncherNavigationPolicy.onBack(
        currentScreen = currentScreen,
        menuVisible = menuVisible,
        settingsReturnScreen = settingsReturnScreen,
        diagnosticsReturnScreen = diagnosticsReturnScreen,
        updateReturnScreen = updateReturnScreen,
      )
    currentScreen = target.screen
    menuVisible = target.menuVisible
  }

  MaterialTheme(colorScheme = DesignTokens.colorScheme(), shapes = DesignTokens.shapes) {
    when (currentScreen) {
      Screen.Home ->
        HomeScreen(
          updateVersion = availableUpdate?.version,
          onStartGame = startGame,
          onOpenSettings = { openSettings(Screen.Home) },
          onOpenUpdate = { openUpdate(Screen.Home) },
          onExit = onExit,
        )

      Screen.Game -> {
        val gamePresentation =
          GameLoadPolicy.resolve(
            hasSession = session != null,
            isLoading = isLoading,
            hasCompletedLoad = hasCompletedLoad,
            failure = GameLoadPolicy.failure(sessionCrashed, pageLoadFailed),
          )
        Box(modifier = Modifier.fillMaxSize()) {
          GameScreen(
            session = session,
            presentation = gamePresentation,
            onRetry = reloadGame,
            onRestore = restoreGame,
          )
          // Restrained overlay dismissal transition; short fade keeps gameplay primary.
          AnimatedVisibility(
            visible = menuVisible,
            enter = fadeIn(tween(140)),
            exit = fadeOut(tween(120)),
          ) {
            GameMenuScreen(
              onReload = {
                reloadGame()
                val target = LauncherNavigationPolicy.afterReload()
                currentScreen = target.screen
                menuVisible = target.menuVisible
              },
              onSettings = { openSettings(Screen.Game) },
              onReturnToGame = {
                val target = LauncherNavigationPolicy.returnToGame()
                currentScreen = target.screen
                menuVisible = target.menuVisible
              },
            )
          }
        }
      }

      Screen.Settings ->
        SettingsScreen(
          context = context,
          usageMeter = usageMeter,
          isCheckingUpdate = isCheckingUpdate,
          onBack = { currentScreen = settingsReturnScreen },
          onOpenDiagnostics = {
            diagnosticsReturnScreen = Screen.Settings
            currentScreen = Screen.Diagnostics
          },
          onOpenUpdates = { openUpdate(Screen.Settings) },
          onCheckUpdates = {
            updateReturnScreen = Screen.Settings
            currentScreen = Screen.Update
            checkForUpdates(true)
          },
        )

      Screen.Diagnostics ->
        DiagnosticsScreen(
          context = context,
          onBack = { currentScreen = diagnosticsReturnScreen },
        )

      Screen.Update ->
        UpdateScreen(
          update = availableUpdate,
          message = updateMessage,
          isChecking = isCheckingUpdate,
          isDownloading = isDownloadingUpdate,
          downloadingDelta = downloadingDelta,
          onBack = { currentScreen = updateReturnScreen },
          onDownload = { preferDelta ->
            val update = availableUpdate
            if (update != null) {
              isDownloadingUpdate = true
              downloadingDelta = preferDelta
              updateMessage = null
              onDownloadUpdate(update, preferDelta) { result ->
                isDownloadingUpdate = false
                result
                  .onSuccess { uri ->
                    availableUpdate = null
                    onInstallUpdate(uri)
                  }
                  .onFailure { error ->
                    updateMessage = error.message ?: "Update download failed"
                  }
              }
            }
          },
        )
    }
  }
}

private fun setupGeckoDelegates(
  geckoSession: GeckoSession,
  context: android.content.Context,
  onLoadState: (Boolean, Boolean) -> Unit,
  onPageLoaded: () -> Unit,
  onSessionCrashed: () -> Unit,
) {
  geckoSession.setProgressDelegate(
    object : GeckoSession.ProgressDelegate {
      override fun onPageStart(session: GeckoSession, url: String) {
        onLoadState(true, false)
        Diagnostics.info(context, TAG, "GeckoView page started")
      }

      override fun onPageStop(session: GeckoSession, success: Boolean) {
        onLoadState(false, !success)
        if (success) onPageLoaded()
        else Diagnostics.warn(context, TAG, "GeckoView page load failed")
      }
    }
  )
  geckoSession.setContentDelegate(
    object : GeckoSession.ContentDelegate {
      override fun onCrash(session: GeckoSession) {
        onSessionCrashed()
        Diagnostics.error(context, TAG, "GeckoView content process crashed")
      }

      override fun onKill(session: GeckoSession) {
        onSessionCrashed()
        Diagnostics.error(context, TAG, "GeckoView content process was killed")
      }

      override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
        Diagnostics.info(context, TAG, "GeckoView fullscreen changed: $fullScreen")
      }

      override fun onExternalResponse(
        session: GeckoSession,
        response: org.mozilla.geckoview.WebResponse,
      ) {
        Diagnostics.warn(context, TAG, "GeckoView external response not handled")
      }
    }
  )
  geckoSession.setNavigationDelegate(
    object : GeckoSession.NavigationDelegate {
      override fun onLoadRequest(
        session: GeckoSession,
        request: GeckoSession.NavigationDelegate.LoadRequest,
      ): GeckoResult<org.mozilla.geckoview.AllowOrDeny> = GeckoResult.allow()
    }
  )
  geckoSession.setPermissionDelegate(
    object : GeckoSession.PermissionDelegate {
      override fun onContentPermissionRequest(
        session: GeckoSession,
        permission: GeckoSession.PermissionDelegate.ContentPermission,
      ): GeckoResult<Int> {
        val host = Uri.parse(permission.uri).host.orEmpty().lowercase(Locale.ROOT)
        val isGameOrigin = host == "lasthavens.com" || host.endsWith(".lasthavens.com")
        val decision =
          when (permission.permission) {
            GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE,
            GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE,
            GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE ->
              if (isGameOrigin) {
                GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
              } else {
                GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY
              }
            else -> GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY
          }
        return GeckoResult.fromValue(decision)
      }

      override fun onAndroidPermissionsRequest(
        session: GeckoSession,
        permissions: Array<out String>?,
        callback: GeckoSession.PermissionDelegate.Callback,
      ) {
        callback.reject()
      }

      override fun onMediaPermissionRequest(
        session: GeckoSession,
        uri: String,
        video: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
        audio: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
        callback: GeckoSession.PermissionDelegate.MediaCallback,
      ) {
        callback.reject()
      }
    }
  )
}
