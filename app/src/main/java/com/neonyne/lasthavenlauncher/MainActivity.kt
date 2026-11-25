package com.neonyne.lasthavenlauncher

import android.content.SharedPreferences
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.util.Locale

private const val DEFAULT_GAME_URL = "https://lasthavens.com/play"
private const val PREFERENCES_NAME = "launcher_settings"
private const val KEY_GAME_URL = "game_url"
private const val TAG = "LastHavenLauncher"

enum class Screen {
    Home,
    Game
}

class MainActivity : ComponentActivity() {
    private var pendingUpdateUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Diagnostics.info(this, "Lifecycle", "Activity created")

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        setContent {
            LastHavenApp(
                preferences = preferences,
                onExit = { finish() },
                onCheckUpdates = { callback -> AppUpdater.checkForUpdate(this, callback) },
                onDownloadUpdate = { update, preferDelta, callback ->
                    AppUpdater.downloadUpdate(this, update, preferDelta, callback)
                },
                onInstallUpdate = { uri -> installUpdate(uri) },
            )
        }
    }

    private fun installUpdate(uri: Uri) {
        if (!packageManager.canRequestPackageInstalls()) {
            pendingUpdateUri = uri
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName"),
                ),
            )
            return
        }

        startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    }

    override fun onResume() {
        super.onResume()
        Diagnostics.info(this, "Lifecycle", "Activity resumed")
        val uri = pendingUpdateUri
        if (uri != null && packageManager.canRequestPackageInstalls()) {
            pendingUpdateUri = null
            installUpdate(uri)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Immersive mode is now managed per-screen in Compose
    }

    private fun applyImmersiveMode() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun clearImmersiveMode() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

}

@Composable
private fun LastHavenApp(
    preferences: SharedPreferences,
    onExit: () -> Unit,
    onCheckUpdates: ((UpdateCheckResult) -> Unit) -> Unit,
    onDownloadUpdate: (AppUpdateInfo, Boolean, (Result<Uri>) -> Unit) -> Unit,
    onInstallUpdate: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val usageMeter = remember { AppUsageMeter(context) }
    val gameUrl = remember {
        preferences.getString(KEY_GAME_URL, DEFAULT_GAME_URL) ?: DEFAULT_GAME_URL
    }

    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var showGameMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var downloadingDelta by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    // GeckoView state - initialized lazily when game starts
    var runtime by remember { mutableStateOf<org.mozilla.geckoview.GeckoRuntime?>(null) }
    var session by remember { mutableStateOf<GeckoSession?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var pageLoadFailed by remember { mutableStateOf(false) }

    val initializeGeckoView = {
        if (runtime == null) {
            runtime = GeckoRuntimeProvider.get(context)
        }
        if (session == null) {
            val newSession = GeckoSession()
            setupGeckoDelegates(newSession!!, context)
            newSession.open(runtime!!)
            session = newSession
        }
    }

    val startGame = {
        currentScreen = Screen.Game
        initializeGeckoView()
        session?.loadUri(gameUrl)
    }

    val reloadGame = {
        if (session?.isOpen() == true) {
            session.reload()
        } else {
            initializeGeckoView()
            session?.loadUri(gameUrl)
        }
    }

    val goHome = {
        showGameMenu = false
        currentScreen = Screen.Home
    }

    LaunchedEffect(Unit) {
        isCheckingUpdate = true
        onCheckUpdates { result ->
            isCheckingUpdate = false
            when (result) {
                is UpdateCheckResult.Available -> availableUpdate = result.update
                is UpdateCheckResult.Failed -> updateMessage = result.message
                UpdateCheckResult.NoUpdate -> Unit
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        when (currentScreen) {
            Screen.Home -> {
                // Restore normal system UI on home screen
                DisposableEffect(currentScreen) {
                    clearImmersiveMode()
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    onDispose {}
                }
                HomeScreen(
                    onStartGame = startGame,
                    onOpenSettings = { showSettingsDialog = true },
                    onExit = onExit,
                    onBackPressed = { /* handled in HomeScreen */ },
                )
            }
            Screen.Game -> {
                // Apply immersive mode and keep-screen-on during gameplay
                DisposableEffect(currentScreen) {
                    applyImmersiveMode()
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    onDispose {
                        clearImmersiveMode()
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
                GameScreen(
                    session = session,
                    isLoading = isLoading,
                    pageLoadFailed = pageLoadFailed,
                    onReload = reloadGame,
                    onShowMenu = { showGameMenu = true },
                )
            }
        }

        if (currentScreen == Screen.Game) {
            BackHandler {
                showGameMenu = true
            }
        }

        if (currentScreen == Screen.Home) {
            BackHandler {
                // Show exit confirmation on home screen
                // Handled inside HomeScreen via callback
            }
        }
    }

    // GeckoView delegates setup
    fun setupGeckoDelegates(geckoSession: GeckoSession, ctx: androidx.compose.ui.platform.LocalContext.current) {
        geckoSession.setProgressDelegate(object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                pageLoadFailed = false
                isLoading = true
                Diagnostics.info(context, TAG, "GeckoView page started: " + url)
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                isLoading = false
                pageLoadFailed = !success
                if (!success) Diagnostics.warn(context, TAG, "GeckoView page load failed")
            }
        })
        geckoSession.setContentDelegate(object : GeckoSession.ContentDelegate {
            override fun onCrash(session: GeckoSession) {
                isLoading = false
                pageLoadFailed = true
                Diagnostics.error(context, TAG, "GeckoView content process crashed")
            }

            override fun onKill(session: GeckoSession) {
                isLoading = false
                pageLoadFailed = true
                Diagnostics.error(context, TAG, "GeckoView content process was killed")
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                Diagnostics.info(context, TAG, "GeckoView fullscreen changed: " + fullScreen)
            }

            override fun onExternalResponse(session: GeckoSession, response: org.mozilla.geckoview.WebResponse) {
                Diagnostics.warn(context, TAG, "GeckoView external response not handled: " + response.uri)
            }
        })
        geckoSession.setNavigationDelegate(object : GeckoSession.NavigationDelegate {
            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest,
            ): GeckoResult<org.mozilla.geckoview.AllowOrDeny> = GeckoResult.allow()
        })
        geckoSession.setPermissionDelegate(object : GeckoSession.PermissionDelegate {
            override fun onContentPermissionRequest(
                session: GeckoSession,
                permission: GeckoSession.PermissionDelegate.ContentPermission,
            ): GeckoResult<Int> {
                val host = Uri.parse(permission.uri).host.orEmpty().lowercase(Locale.ROOT)
                val isGameOrigin = host == "lasthavens.com" || host.endsWith(".lasthavens.com")
                val decision = when (permission.permission) {
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
        })
    }

    // Update dialogs
    if (availableUpdate != null) {
        val update = availableUpdate!!
        AlertDialog(
            onDismissRequest = { if (!isDownloadingUpdate) availableUpdate = null },
            title = { Text("Update available") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("LastHavenPlus ${update.version} is available.")
                    if (update.patch != null) {
                        Text(
                            "A smaller differential download is available " +
                                "(${"%.1f".format(update.patch.size / 1024.0 / 1024.0)} MiB).",
                        )
                    }
                    Text(update.releaseNotes, maxLines = 8)
                    if (isDownloadingUpdate) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(if (downloadingDelta) "Downloading smaller update…" else "Downloading full APK…")
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isDownloadingUpdate,
                    onClick = {
                        isDownloadingUpdate = true
                        downloadingDelta = update.patch != null
                        onDownloadUpdate(update, update.patch != null) { result ->
                            isDownloadingUpdate = false
                            result.onSuccess { uri ->
                                availableUpdate = null
                                onInstallUpdate(uri)
                            }.onFailure { error ->
                                updateMessage = error.message ?: "Update download failed"
                            }
                        }
                    },
                ) {
                    Text(
                        when {
                            isDownloadingUpdate -> "Downloading…"
                            update.patch != null -> "Download smaller update"
                            else -> "Download update"
                        },
                    )
                }
            },
            dismissButton = {
                Column(horizontalAlignment = Alignment.End) {
                    if (update.patch != null) {
                        TextButton(
                            enabled = !isDownloadingUpdate,
                            onClick = {
                                isDownloadingUpdate = true
                                downloadingDelta = false
                                onDownloadUpdate(update, false) { result ->
                                    isDownloadingUpdate = false
                                    result.onSuccess { uri ->
                                        availableUpdate = null
                                        onInstallUpdate(uri)
                                    }.onFailure { error ->
                                        updateMessage = error.message ?: "Full APK download failed"
                                    }
                                }
                            },
                        ) { Text("Download full APK instead") }
                    }
                    TextButton(
                        enabled = !isDownloadingUpdate,
                        onClick = { availableUpdate = null },
                    ) { Text("Later") }
                }
            },
        )
    }

    if (updateMessage != null) {
        AlertDialog(
            onDismissRequest = { updateMessage = null },
            title = { Text("Update check") },
            text = { Text(updateMessage!!) },
            confirmButton = {
                TextButton(onClick = { updateMessage = null }) { Text("OK") }
            },
        )
    }

    if (showGameMenu && currentScreen == Screen.Game) {
        GameMenuDialog(
            onReload = reloadGame,
            onHome = goHome,
            onReturnToGame = { showGameMenu = false },
            onSettings = { showGameMenu = false; showSettingsDialog = true },
            onDiagnostics = { showGameMenu = false; showDiagnosticsDialog = true },
            onExit = {
                showGameMenu = false
                onExit()
            },
            onDismiss = { showGameMenu = false },
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(context, usageMeter) { showSettingsDialog = false }
    }

    if (showDiagnosticsDialog) {
        DiagnosticsDialog(context) { showDiagnosticsDialog = false }
    }
}
