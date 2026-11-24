package com.neonyne.lasthavenlauncher

import android.content.SharedPreferences
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.WindowManager
import android.webkit.ValueCallback
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.cefrium.CefriumBrowser
import com.cefrium.CefriumExperimental
import com.cefrium.CefriumLoadError
import com.cefrium.CefriumNavigationClient
import com.cefrium.compose.CefriumView
import com.cefrium.compose.rememberCefriumNavigator
import com.cefrium.compose.rememberCefriumViewState

private const val DEFAULT_GAME_URL = "https://alt.lasthavens.com/play"
private const val PREFERENCES_NAME = "launcher_settings"
private const val KEY_GAME_URL = "game_url"
private const val TAG = "LastHavenLauncher"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applyImmersiveMode()

        val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        setContent {
            LastHavenApp(
                preferences = preferences,
                onExit = { finish() },
                onCheckUpdates = { callback -> AppUpdater.checkForUpdate(this, callback) },
                onDownloadUpdate = { update, callback -> AppUpdater.downloadUpdate(this, update, callback) },
                onInstallUpdate = { uri ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                },
            )
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

@OptIn(CefriumExperimental::class)
@Composable
private fun LastHavenApp(
    preferences: SharedPreferences,
    onExit: () -> Unit,
    onCheckUpdates: ((UpdateCheckResult) -> Unit) -> Unit,
    onDownloadUpdate: (AppUpdateInfo, (Result<Uri>) -> Unit) -> Unit,
    onInstallUpdate: (Uri) -> Unit,
) {
    val gameUrl = remember {
        preferences.getString(KEY_GAME_URL, DEFAULT_GAME_URL) ?: DEFAULT_GAME_URL
    }
    val browserState = rememberCefriumViewState(gameUrl)
    val navigator = rememberCefriumNavigator()
    var showExitPrompt by remember { mutableStateOf(false) }
    var pageLoadFailed by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

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
                CefriumView(
                    state = browserState,
                    modifier = Modifier.fillMaxSize(),
                    navigator = navigator,
                    onBrowserReady = { browser ->
                        var persistentStorageRequested = false
                        browser.setNavigationClient(object : CefriumNavigationClient {
                            override fun onLoadStart(browser: CefriumBrowser, url: String) {
                                pageLoadFailed = false
                            }

                            override fun onLoadError(
                                browser: CefriumBrowser,
                                error: CefriumLoadError,
                            ) {
                                pageLoadFailed = true
                                Log.w(TAG, "Main-frame navigation failed: $error")
                            }

                            override fun onLoadingStateChanged(
                                browser: CefriumBrowser,
                                loading: Boolean,
                                canGoBack: Boolean,
                                canGoForward: Boolean,
                            ) {
                                if (!loading && !persistentStorageRequested) {
                                    persistentStorageRequested = true
                                    browser.evaluateJavaScript(
                                        "navigator.storage && navigator.storage.persist " +
                                            "? navigator.storage.persist() : Promise.resolve(false)",
                                        ValueCallback { result ->
                                            Log.i(TAG, "Persistent storage request result: $result")
                                        },
                                    )
                                }
                            }
                        })
                    },
                )

                if (browserState.isLoading && !pageLoadFailed) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(),
                    )
                }

                if (pageLoadFailed || browserState.rendererTermination != null) {
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
                                navigator.reload()
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
        if (browserState.canGoBack) {
            navigator.navigateBack()
        } else {
            showExitPrompt = true
        }
    }

    if (availableUpdate != null) {
        val update = availableUpdate!!
        AlertDialog(
            onDismissRequest = { if (!isDownloadingUpdate) availableUpdate = null },
            title = { Text("Update available") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("LastHaven Launcher ${update.version} is available.")
                    Text(update.releaseNotes, maxLines = 8)
                    if (isDownloadingUpdate) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Downloading update…")
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isDownloadingUpdate,
                    onClick = {
                        isDownloadingUpdate = true
                        onDownloadUpdate(update) { result ->
                            isDownloadingUpdate = false
                            result.onSuccess { uri ->
                                availableUpdate = null
                                onInstallUpdate(uri)
                            }.onFailure { error ->
                                updateMessage = error.message ?: "Update download failed"
                            }
                        }
                    },
                ) { Text(if (isDownloadingUpdate) "Downloading…" else "Download update") }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDownloadingUpdate,
                    onClick = { availableUpdate = null },
                ) { Text("Later") }
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
                        navigator.reload()
                    }) { Text("Reload game") }
                }
            },
        )
    }
}
