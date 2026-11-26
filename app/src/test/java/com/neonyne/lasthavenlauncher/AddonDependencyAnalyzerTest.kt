package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddonDependencyAnalyzerTest {
  @Test
  fun reportsMissingAndDisabledDependencies() {
    val report =
      AddonDependencyAnalyzer.analyze(
        listOf(
          addon("Alpha", dependencies = listOf("Beta", "Missing")),
          addon("Beta", enabled = false),
        )
      )

    assertEquals(listOf("Beta", "Missing"), report.missingByFolder["Alpha"])
    assertTrue(report.missingByFolder["Beta"].orEmpty().isEmpty())
    assertTrue(report.cyclicFolders.isEmpty())
  }

  @Test
  fun identifiesOnlyMembersOfDependencyCycle() {
    val report =
      AddonDependencyAnalyzer.analyze(
        listOf(
          addon("Alpha", dependencies = listOf("Beta")),
          addon("Beta", dependencies = listOf("Alpha")),
          addon("Consumer", dependencies = listOf("Alpha")),
        )
      )

    assertEquals(setOf("Alpha", "Beta"), report.cyclicFolders)
  }

  @Test
  fun handlesDeepDependencyChainsWithoutRecursion() {
    val addons =
      (0 until 5_000).map { index ->
        addon(
          folder = "Addon$index",
          dependencies = if (index == 0) emptyList() else listOf("Addon${index - 1}"),
        )
      }

    val report = AddonDependencyAnalyzer.analyze(addons)

    assertTrue(report.cyclicFolders.isEmpty())
    assertTrue(report.missingByFolder.values.all { it.isEmpty() })
  }

  private fun addon(
    folder: String,
    enabled: Boolean = true,
    dependencies: List<String> = emptyList(),
  ) =
    AddonLibraryItem(
      title = folder,
      folder = folder,
      version = "1",
      interfaceVersion = "30300",
      notes = "",
      dependencies = dependencies,
      sizeBytes = 1,
      filesCount = 1,
      enabled = enabled,
      importedAt = 0,
      warnings = emptyList(),
    )
}
