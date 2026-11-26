package com.neonyne.lasthavenlauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddonManagerScreen(
  snapshot: AddonLibrarySnapshot,
  busy: Boolean,
  message: String?,
  onImport: () -> Unit,
  onToggle: (String, Boolean) -> Unit,
  onExport: (String) -> Unit,
  onRemove: (String) -> Unit,
  onBack: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          "ADDON MANAGER",
          color = DesignTokens.frost,
          style = MaterialTheme.typography.labelSmall,
        )
        Text("Local addons", style = MaterialTheme.typography.headlineSmall)
      }
      OutlinedButton(
        onClick = onBack,
        enabled = !busy,
        modifier = Modifier.heightIn(min = DesignTokens.minTouchTarget),
      ) {
        Text("Back")
      }
    }

    Text(
      "Import local WoW 3.3.5a addon ZIP files. New imports start disabled and stay on this device. Runtime loading is not enabled in this draft.",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style = MaterialTheme.typography.bodyMedium,
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Button(
        onClick = onImport,
        enabled = !busy,
        modifier = Modifier.weight(1f).heightIn(min = DesignTokens.minTouchTarget),
        shape = RoundedCornerShape(DesignTokens.cornerButton),
        colors = DesignTokens.primaryButtonColors(),
      ) {
        Text("Import ZIP")
      }
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(DesignTokens.cornerButton),
      ) {
        Text(
          "${snapshot.addons.size} addons\n${formatAddonSize(snapshot.totalBytes)}",
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }

    if (!message.isNullOrBlank()) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(DesignTokens.cornerButton),
      ) {
        Text(
          message,
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          style = MaterialTheme.typography.bodySmall,
        )
      }
    }

    if (snapshot.addons.isEmpty()) {
      Spacer(Modifier.height(20.dp))
      Text(
        "No local addons yet. Import a ZIP containing an addon folder and its matching .toc file.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
      )
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(snapshot.addons, key = { it.folder }) { addon ->
          AddonCard(
            addon = addon,
            library = snapshot,
            busy = busy,
            onToggle = { enabled -> onToggle(addon.folder, enabled) },
            onExport = { onExport(addon.folder) },
            onRemove = { onRemove(addon.folder) },
          )
        }
      }
    }

    Text(
      "Addon files remain local. Runtime loading and same-session game integration are not available in this draft.",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style = MaterialTheme.typography.bodySmall,
    )
  }
}

@Composable
private fun AddonCard(
  addon: AddonLibraryItem,
  library: AddonLibrarySnapshot,
  busy: Boolean,
  onToggle: (Boolean) -> Unit,
  onExport: () -> Unit,
  onRemove: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(DesignTokens.cornerCard),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            addon.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            "${addon.folder} · ${addon.version.ifBlank { "No version" }}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
          )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
          checked = addon.enabled,
          onCheckedChange = onToggle,
          enabled = !busy,
        )
      }

      Text(
        "${addon.filesCount} files · ${formatAddonSize(addon.sizeBytes)}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
      )

      val missing =
        addon.dependencies.filter { dependency ->
          library.addons.none { it.folder.equals(dependency, ignoreCase = true) && it.enabled }
        }
      if (missing.isNotEmpty()) {
        Text(
          "Check required dependencies: ${missing.joinToString()}",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
        )
      }
      addon.warnings.forEach { warning ->
        Text(
          warning,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
        )
      }
      if (addon.notes.isNotBlank()) {
        Text(addon.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = onExport,
          enabled = !busy,
          modifier = Modifier.heightIn(min = DesignTokens.minTouchTarget),
        ) {
          Text("Export ZIP")
        }
        OutlinedButton(
          onClick = onRemove,
          enabled = !busy,
          modifier = Modifier.heightIn(min = DesignTokens.minTouchTarget),
        ) {
          Text("Remove")
        }
      }
    }
  }
}

private fun formatAddonSize(bytes: Long): String =
  if (bytes < 1024L) {
    "$bytes B"
  } else {
    String.format(java.util.Locale.ROOT, "%.1f MiB", bytes / (1024.0 * 1024.0))
  }
