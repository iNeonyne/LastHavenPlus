package com.lasthaven.launcher

import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.pointerInput
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.setDecorFitsSystemWindows
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cefrium.Cefrium
import com.cefrium.compose.BackgroundMediaPolicy
import com.cefrium.compose.CefriumView
import com.cefrium.compose.CefriumViewState
import com.cefrium.compose.rememberCefriumViewState
import com.lasthaven.launcher.ui.theme.LastHavenLauncherTheme
import com.cefrium.navigation.CefriumNavigationClient
import com.cefrium.navigation.CefriumLoadError
import com.cefrium.navigation.CefriumNavigationRequest
import com.cefrium.chrome.CefriumChromeClient
import com.cefrium.chrome.CefriumFullscreenChange
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge for fullscreen gameplay
        setDecorFitsSystemWindows(false)
        enableEdgeToEdge()
        setContent {
            LastHavenLauncherTheme(darkTheme = true) {
                GameScreen(
                    viewModel = viewModel,
                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun enableEdgeToEdge() {
        window.setDecorFitsSystemWindows(false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}

class MainViewModel(private val settingsStore: SettingsStore) : ViewModel() {

    // Game URL - loaded from settings
    val gameUrl = mutableStateOf(settingsStore.getGameUrl().await())

    // Loading state
    var isLoading by mutableStateOf(true)
    var loadError: String? = null
        private set

    // Fullscreen state (synced with game)
    var isFullscreen by mutableStateOf(false)

    // Cefrium state reference for settings screen
    var cefriumState: CefriumViewState? = null
        private set

    // Navigation client for error handling
    val navigationClient = rememberCefriumNavigationClient()

    // Chrome client for fullscreen handling
    val chromeClient = rememberCefriumChromeClient()

    private fun rememberCefriumNavigationClient(): CefriumNavigationClient {
        return object : CefriumNavigationClient {
            override fun onLoadStart(browser: com.cefrium.CefriumBrowser, url: String) {
                isLoading = true
                loadError = null
            }

            override fun onLoadError(browser: com.cefrium.CefriumBrowser, error: CefriumLoadError) {
                isLoading = false
                loadError = "${error.errorCode}: ${error.errorText}"
                // Auto-retry on network errors after delay
                if (isNetworkError(error.errorCode)) {
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        browser.reload()
                    }
                }
            }

            override fun onLoadingStateChanged(
                browser: com.cefrium.CefriumBrowser,
                loading: Boolean,
                canGoBack: Boolean,
                canGoForward: Boolean
            ) {
                if (!loading) isLoading = false
            }

            override fun onRenderProcessTerminated(
                browser: com.cefrium.CefriumBrowser,
                status: com.cefrium.navigation.CefriumRendererTerminationStatus,
                errorCode: Int
            ) {
                // Renderer crashed - reload after short delay
                isLoading = true
                loadError = "Renderer terminated: $status ($errorCode)"
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1000)
                    browser.reload()
                }
            }
        }
    }

    private fun rememberCefriumChromeClient(): CefriumChromeClient {
        return object : CefriumChromeClient {
            override fun onFullscreenChanged(browser: com.cefrium.CefriumBrowser, fullscreen: Boolean) {
                isFullscreen = fullscreen
                updateSystemBarsVisibility(fullscreen)
            }
        }
    }

    private fun updateSystemBarsVisibility(fullscreen: Boolean) {
        // Handled in Compose via window insets
    }

    private fun isNetworkError(errorCode: Int): Boolean {
        // Chromium net error codes for network failures
        return errorCode in setOf(
            -101, // ERR_CONNECTION_REFUSED
            -102, // ERR_CONNECTION_RESET
            -103, // ERR_CONNECTION_CLOSED
            -104, // ERR_CONNECTION_FAILED
            -105, // ERR_NAME_NOT_RESOLVED
            -106, // ERR_INTERNET_DISCONNECTED
            -107, // ERR_SSL_PROTOCOL_ERROR
            -108, // ERR_SSL_VERSION_OR_CIPHER_MISMATCH
            -109, // ERR_SSL_CLIENT_AUTH_CERT_NEEDED
            -110, // ERR_SSL_PINNED_KEY_NOT_IN_CERT_CHAIN
            -111, // ERR_SSL_VERSION_INTERFERENCE
            -112, // ERR_SSL_DECRYPT_ERROR_ALERT
            -113, // ERR_SSL_UNRECOGNIZED_NAME_ALERT
            -114, // ERR_SSL_BAD_RECORD_MAC_ALERT
            -115, // ERR_SSL_WEAK_SERVER_EPHEMERAL_DH_KEY
            -116, // ERR_SSL_SERVER_CERT_CHANGED
            -117, // ERR_SSL_UNRECOGNIZED_NAME_ALERT
            -200, // ERR_CERT_COMMON_NAME_INVALID
            -201, // ERR_CERT_DATE_INVALID
            -202, // ERR_CERT_AUTHORITY_INVALID
            -203, // ERR_CERT_CONTAINS_ERRORS
            -204, // ERR_CERT_NO_REVOCATION_MECHANISM
            -205, // ERR_CERT_UNABLE_TO_CHECK_REVOCATION
            -206, // ERR_CERT_REVOKED
            -207, // ERR_CERT_INVALID
            -208, // ERR_CERT_WEAK_SIGNATURE_ALGORITHM
            -209, // ERR_CERT_NON_UNIQUE_NAME
            -210, // ERR_CERT_WEAK_KEY
            -211, // ERR_CERT_NAME_CONSTRAINT_VIOLATION
            -212, // ERR_CERT_VALIDITY_TOO_LONG
            -213, // ERR_CERT_TRANSPARENCY_REQUIRED
            -214, // ERR_CERT_SYMANTEC_LEGACY
            -215, // ERR_CERT_KNOWN_PINNED_KEY_NOT_IN_CERT_CHAIN
            -300, // ERR_NETWORK_CHANGED
            -301, // ERR_NETWORK_IO_SUSPENDED
            -302  // ERR_NETWORK_IO_SUSPENDED
        )
    }
}

@Composable
fun GameScreen(
    viewModel: MainViewModel,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val gameUrl by viewModel.gameUrl
    val isLoading by viewModel.isLoading
    val loadError by viewModel.loadError
    val isFullscreen by viewModel.isFullscreen
    var showSettings by remember { mutableStateOf(false) }

    // CefriumView state - persists across recompositions
    val cefriumState = rememberCefriumViewState(
        url = gameUrl,
        incognito = false,
        profile = null // Uses default profile
    )

    // Store reference for settings screen access
    viewModel.cefriumState = cefriumState

    // Apply settings changes to browser
    androidx.compose.runtime.LaunchedEffect(gameUrl) {
        if (cefriumState.browser != null && cefriumState.browser!!.url != gameUrl) {
            cefriumState.browser!!.loadUrl(gameUrl)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CefriumView(
            state = cefriumState,
            modifier = Modifier.fillMaxSize(),
            navigationClient = viewModel.navigationClient,
            chromeClient = viewModel.chromeClient,
            backgroundMediaPolicy = BackgroundMediaPolicy.PAUSE_ON_STOP,
            onBrowserReady = { browser ->
                // Browser is ready, we could inject JS here in Phase 2 (addons)
            }
        ) { url ->
            // Navigation happened - could track if needed
        }

        // Settings access: long-press anywhere when not loading/error
        if (!isLoading && loadError == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showSettings = true },
                            onPress = { /* Consume press */ }
                        )
                    }
            )
        }

        // Loading overlay
        if (isLoading || loadError != null) {
            LoadingOverlay(
                isLoading = isLoading,
                error = loadError,
                onRetry = {
                    viewModel.loadError = null
                    cefriumState.browser?.reload()
                }
            )
        }

        // Settings FAB (only when not in fullscreen gameplay)
        if (!isFullscreen && !isLoading && loadError == null) {
            FloatingActionButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFF4A90D9),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings)
                )
            }
        }
    }

    // Settings screen overlay
    if (showSettings) {
        SettingsScreen(onClose = { showSettings = false }, viewModel = viewModel)
    }
}

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    androidx.compose.material3.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Card(
            elevation = androidx.compose.ui.graphics.drawscope.drawContext.canvas.nativeCanvas.elevation + 8.dp,
            modifier = Modifier.padding(24.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFF4A90D9),
                        strokeWidth = 4.dp
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.loading_game),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                } else {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFF6B6B)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.connection_error),
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.TextAlign.Center
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                    androidx.compose.material3.Text(
                        text = error ?: "Unknown error",
                        fontSize = 12.sp,
                        color = Color(0xFFBBBBBB),
                        textAlign = androidx.compose.ui.text.TextAlign.Center,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 24.dp))
                    androidx.compose.material3.Button(
                        onClick = onRetry,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A90D9)
                        )
                    ) {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.retry),
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Theme placeholder - will be created next

