package com.neonyne.lasthavenlauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@Composable
fun GameScreen(
  session: GeckoSession?,
  presentation: GameLoadPresentation,
  onRetry: () -> Unit,
  onRestore: () -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize()) {
    if (session != null) {
      AndroidView(
        factory = { viewContext -> GeckoView(viewContext).apply { setSession(session) } },
        modifier = Modifier.fillMaxSize(),
        // Detach the view but keep the GeckoSession open when navigating to another screen.
        onRelease = { view -> view.releaseSession() },
      )
    }

    when (presentation) {
      GameLoadPresentation.Game -> Unit

      GameLoadPresentation.Preparing ->
        GameStatusSurface(contentDescription = "Preparing LastHaven") {
          LinearProgressIndicator()
          Spacer(modifier = Modifier.height(16.dp))
          Text("Preparing game…", color = DesignTokens.textSecondary)
        }

      GameLoadPresentation.LoadingGame ->
        // Restrained branded state for the initial page load only; no fabricated progress.
        GameStatusSurface(contentDescription = "LastHaven is loading") {
          LinearProgressIndicator(color = DesignTokens.gold)
          Spacer(modifier = Modifier.height(16.dp))
          Text("Loading LastHaven…", color = DesignTokens.textSecondary)
        }

      GameLoadPresentation.RecoverableFailure ->
        GameStatusSurface(contentDescription = "LastHaven could not load") {
          Text(
            "Unable to load LastHaven",
            style = MaterialTheme.typography.headlineSmall,
            color = DesignTokens.textPrimary,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "Check your connection and try again.",
            color = DesignTokens.textSecondary,
            modifier = Modifier.widthIn(max = 320.dp).padding(horizontal = 8.dp),
          )
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = onRetry,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(DesignTokens.cornerButton),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = DesignTokens.gold,
                contentColor = DesignTokens.goldOnColor,
              ),
          ) {
            Text("Retry", fontWeight = FontWeight.Bold)
          }
        }

      GameLoadPresentation.RendererFailure ->
        GameStatusSurface(contentDescription = "LastHaven stopped unexpectedly") {
          Text(
            "LastHaven stopped unexpectedly",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "The game session ended. Restore the game to continue playing.",
            color = DesignTokens.textSecondary,
            modifier = Modifier.widthIn(max = 320.dp).padding(horizontal = 8.dp),
          )
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = onRestore,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = DesignTokens.gold,
                contentColor = Color(0xFF17130A),
              ),
          ) {
            Text("Restore game", fontWeight = FontWeight.Bold)
          }
        }
    }
  }
}

@Composable
private fun GameStatusSurface(
  contentDescription: String,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxSize().semantics { this.contentDescription = contentDescription },
    color = DesignTokens.logSurface,
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      content()
    }
  }
}
