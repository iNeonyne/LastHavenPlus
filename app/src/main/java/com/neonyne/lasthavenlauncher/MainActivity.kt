package com.neonyne.lasthavenlauncher

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File
import java.util.concurrent.Executors

private const val DEFAULT_GAME_URL = "https://lasthavens.com/play"
private const val PREFERENCES_NAME = "launcher_settings"
private const val KEY_GAME_URL = "game_url"
private const val KEY_PENDING_UPDATE_URI = "pending_update_uri"

enum class Screen {
  Home,
  Game,
  Settings,
  Diagnostics,
  Update,
  AddonManager,
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

  val gameSessionController = remember { GameSessionController() }
  val addonLibraryRepository =
    remember(context) { AddonLibraryRepository(File(context.filesDir, "addons")) }
  val addonBridgeExecutor = remember { Executors.newSingleThreadExecutor() }
  val addonBridgeController =
    remember(addonLibraryRepository, addonBridgeExecutor) {
      AddonBridgeController(
        snapshotProvider = { addonLibraryRepository.readLibrary() },
        payloadProvider = AddonRuntimePayloadProvider(addonLibraryRepository),
        worker = addonBridgeExecutor,
      )
    }

  // Install the built-in extension before loading the game URL, then bind the exact session
  // before its first navigation so document_start scripts and native sender checks are ready.
  // The bridge reuses the process-scoped runtime and never creates a second GeckoSession.
  DisposableEffect(addonBridgeController, addonBridgeExecutor) {
    onDispose {
      addonBridgeController.teardown()
      addonBridgeExecutor.shutdown()
    }
  }

  // Session keep-alive invariant: visiting Settings, Diagnostics, Update or the Addon Manager
  // detaches the GeckoView (GameScreen releases its view binding) but must not close the live
  // game session. This effect is the only place that closes a session, and it does so only when
  // the controller replaces the session (crash restore) or when this composition root leaves.
  DisposableEffect(gameSessionController.session) {
    val sessionToClose = gameSessionController.session
    onDispose { sessionToClose?.close() }
  }

  val reloadGame: () -> Unit = {
    addonBridgeController.ensureInstalled(context) {
      addonBridgeController.refreshSnapshot {
        gameSessionController.reloadGame(context, gameUrl)
      }
    }
  }

  val startGame: () -> Unit = {
    addonBridgeController.ensureInstalled(context) {
      gameSessionController.startGame(context, gameUrl) { session ->
        addonBridgeController.setGameSession(session)
      }
      menuVisible = false
      currentScreen = Screen.Game
    }
  }

  val restoreGame: () -> Unit = {
    addonBridgeController.ensureInstalled(context) {
      addonBridgeController.refreshSnapshot {
        gameSessionController.restoreGame(context, gameUrl) { session ->
          addonBridgeController.setGameSession(session)
        }
      }
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
    // Start/resume the game immediately. Update checks are explicit from the Update screen.
    startGame()
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

  MaterialTheme(
    colorScheme = DesignTokens.colorScheme(),
    shapes = DesignTokens.shapes,
    typography = DesignTokens.typography(),
  ) {
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
        var animatorDurationScale by remember { mutableFloatStateOf(1f) }
        DisposableEffect(context) {
          val resolver = context.contentResolver
          val readAnimatorScale =
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
          val listener =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
              override fun onChange(selfChange: Boolean) {
                animatorDurationScale =
                  Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
              }
            }
          animatorDurationScale = readAnimatorScale
          resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            listener,
          )
          onDispose { resolver.unregisterContentObserver(listener) }
        }
        val gamePresentation = gameSessionController.uiState.presentation
        Box(modifier = Modifier.fillMaxSize()) {
          GameScreen(
            session = gameSessionController.session,
            presentation = gamePresentation,
            onRetry = reloadGame,
            onRestore = restoreGame,
          )
          AnimatedVisibility(
            visible = menuVisible,
            enter =
              fadeIn(
                animationSpec =
                  tween(durationMillis = OverlayMotionPolicy.fadeInMillis(animatorDurationScale))
              ),
            exit =
              fadeOut(
                animationSpec =
                  tween(durationMillis = OverlayMotionPolicy.fadeOutMillis(animatorDurationScale))
              ),
          ) {
            GameMenuScreen(
              onReload = {
                reloadGame()
                val target = LauncherNavigationPolicy.afterReload()
                currentScreen = target.screen
                menuVisible = target.menuVisible
              },
              onSettings = { openSettings(Screen.Game) },
              onAddonManager = {
                val target = LauncherNavigationPolicy.openAddonManager()
                currentScreen = target.screen
                menuVisible = target.menuVisible
              },
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

      Screen.AddonManager ->
        AddonManagerHost(
          onBack = {
            currentScreen = LauncherNavigationPolicy.returnToGame().screen
          }
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
