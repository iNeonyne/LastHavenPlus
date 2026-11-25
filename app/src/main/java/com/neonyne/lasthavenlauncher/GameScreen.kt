package com.neonyne.lasthavenlauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@Composable
fun GameScreen(
    session: GeckoSession?,
    pageLoadFailed: Boolean,
    onReload: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (session != null) {
            AndroidView(
                factory = { viewContext -> GeckoView(viewContext).apply { setSession(session) } },
                modifier = Modifier.fillMaxSize(),
                // Detach the view but keep the GeckoSession open when navigating to another screen.
                onRelease = { view -> view.releaseSession() },
            )
        } else {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080B12)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    LinearProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Preparing game…", color = Color(0xFFB7C0D0))
                }
            }
        }

        if (pageLoadFailed && session != null) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080B12)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Unable to load LastHaven",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Check your connection and try again.", color = Color(0xFFB7C0D0))
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onReload) { Text("Retry") }
                }
            }
        }
    }
}
