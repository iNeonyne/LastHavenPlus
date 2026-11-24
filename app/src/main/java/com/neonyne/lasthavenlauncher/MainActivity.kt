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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

class MainActivity : ComponentActivity() {
    private var pendingUpdateUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Diagnostics.info(this, "Lifecycle", "Activity created")

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applyImmersiveMode()

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
        if (hasFocus) applyImmersiveMode()
    }

    private fun applyImmersiveMode() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
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
    val runtime = remember(context) { GeckoRuntimeProvider.get(context) }
    val session = remember(runtime) { GeckoSession() }
    var isLoading by remember { mutableStateOf(true) }
    var showExitPrompt by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var pageLoadFailed by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var downloadingDelta by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    DisposableEffect(session, runtime, gameUrl) {
        session.setProgressDelegate(object : GeckoSession.ProgressDelegate {
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
        session.setContentDelegate(object : GeckoSession.ContentDelegate {
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
        session.setNavigationDelegate(object : GeckoSession.NavigationDelegate {
            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest,
            ): GeckoResult<org.mozilla.geckoview.AllowOrDeny> = GeckoResult.allow()
        })
        session.setPermissionDelegate(object : GeckoSession.PermissionDelegate {
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
        session.open(runtime)
        session.loadUri(gameUrl)
        onDispose {
            session.close()
        }
    }

    val reloadGame = {
        if (session.isOpen) {
            session.reload()
        } else {
            session.open(runtime)
            session.loadUri(gameUrl)
        }
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
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { viewContext ->
                        GeckoView(viewContext).apply { setSession(session) }
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { view -> view.releaseSession() },
                )

                if (isLoading && !pageLoadFailed) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(),
                    )
                }

                if (pageLoadFailed) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF080B12),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Unable to load LastHaven",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check your connection and try again.",
                                color = Color(0xFFB7C0D0),
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = {
                                pageLoadFailed = false
                                reloadGame()
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }

    BackHandler {
        showExitPrompt = true
    }

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

    if (showExitPrompt) {
        AlertDialog(
            onDismissRequest = { showExitPrompt = false },
            title = { Text("Leave LastHaven?") },
            text = { Text("Your game data remains stored on this device.") },
            confirmButton = {
                Button(onClick = {
                    showExitPrompt = false
                    onExit()
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = { showExitPrompt = false }) { Text("Return to game") }
                    TextButton(onClick = {
                        isCheckingUpdate = true
                        onCheckUpdates { result ->
                            isCheckingUpdate = false
                            when (result) {
                                is UpdateCheckResult.Available -> availableUpdate = result.update
                                is UpdateCheckResult.Failed -> updateMessage = result.message
                                UpdateCheckResult.NoUpdate -> updateMessage = "You are using the latest version."
                            }
                        }
                    }) { Text(if (isCheckingUpdate) "Checking…" else "Check for updates") }
                    TextButton(onClick = {
                        showExitPrompt = false
                        showSettingsDialog = true
                    }) { Text("Settings & usage") }
                    TextButton(onClick = {
                        showExitPrompt = false
                        showDiagnosticsDialog = true
                    }) { Text("App diagnostics") }
                    TextButton(onClick = {
                        showExitPrompt = false
                        navigator.reload()
                    }) { Text("Reload game") }
                }
            },
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(context, usageMeter) { showSettingsDialog = false }
    }

    if (showDiagnosticsDialog) {
        DiagnosticsDialog(context) { showDiagnosticsDialog = false }
    }
}
