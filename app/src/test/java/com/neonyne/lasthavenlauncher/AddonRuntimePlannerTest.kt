package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddonRuntimePlannerTest {
  private fun item(
    folder: String,
    enabled: Boolean = true,
    dependencies: List<String> = emptyList(),
    optionalDependencies: List<String> = emptyList(),
  ) =
    AddonLibraryItem(
      title = folder,
      folder = folder,
      version = "1.0.0",
      interfaceVersion = "30300",
      notes = "",
      dependencies = dependencies,
      sizeBytes = 128L,
      filesCount = 2,
      enabled = enabled,
      importedAt = 1000L,
      warnings = emptyList(),
      optionalDependencies = optionalDependencies,
    )

  @Test
  fun dependenciesLoadBeforeDependentsInStableOrder() {
    val plan =
      AddonRuntimePlanner.plan(
        AddonLibrarySnapshot(
          "revision-one",
          listOf(
            item("BagSort", dependencies = listOf("AceCore")),
            item("AceCore"),
            item("QuestHelper", dependencies = listOf("BagSort")),
          ),
        )
      )
    assertEquals(
      listOf("AceCore", "BagSort", "QuestHelper"),
      plan.orderedAddons.map { it.folder },
    )
    assertTrue(plan.blockedAddons.isEmpty())
  }

  @Test
  fun missingDependenciesPropagateAndDisabledAddonsAreExcluded() {
    val plan =
      AddonRuntimePlanner.plan(
        AddonLibrarySnapshot(
          "revision-two",
          listOf(
            item("DisabledLib", enabled = false),
            item("MidLib", dependencies = listOf("DisabledLib")),
            item("TopAddon", dependencies = listOf("MidLib")),
            item("HealthyAddon"),
          ),
        )
      )
    assertEquals(listOf("HealthyAddon"), plan.orderedAddons.map { it.folder })
    assertTrue(plan.blockedAddons["MidLib"] is AddonBlockedReason.MissingDependencies)
    assertTrue(plan.blockedAddons["TopAddon"] is AddonBlockedReason.MissingDependencies)
  }

  @Test
  fun dependencyCyclesAreBlockedWithoutBlockingIndependentAddons() {
    val plan =
      AddonRuntimePlanner.plan(
        AddonLibrarySnapshot(
          "revision-three",
          listOf(
            item("Alpha", dependencies = listOf("Beta")),
            item("Beta", dependencies = listOf("Alpha")),
            item("Standalone"),
          ),
        )
      )
    assertEquals(listOf("Standalone"), plan.orderedAddons.map { it.folder })
    assertEquals(AddonBlockedReason.DependencyCycle, plan.blockedAddons["Alpha"])
    assertEquals(AddonBlockedReason.DependencyCycle, plan.blockedAddons["Beta"])
  }

  @Test
  fun tocPathsIgnoreCommentsAndRejectUnsafeOrDuplicateEntries() {
    val files =
      AddonRuntimePlanner.parseTocLoadFiles(
        """
        ## Interface: 30300
        # Comment
        Core\Init.lua
        Core/Init.lua
        ../escape.lua
        /absolute.lua
        UI/Panel.xml
        """
          .trimIndent()
      )
    assertEquals(listOf("Core/Init.lua", "UI/Panel.xml"), files)
  }

  @Test
  fun emptyPlanAndRevisionAreDeterministicForSameSnapshot() {
    val snapshot = AddonLibrarySnapshot("empty-revision", emptyList())
    val first = AddonRuntimePlanner.plan(snapshot)
    val second = AddonRuntimePlanner.plan(snapshot)
    assertTrue(first.isEmpty)
    assertEquals(first.bundleRevision, second.bundleRevision)

    val enabled =
      AddonRuntimePlanner.plan(AddonLibrarySnapshot("r1", listOf(item("Addon"))))
    val changed =
      AddonRuntimePlanner.plan(AddonLibrarySnapshot("r2", listOf(item("Addon"))))
    assertNotEquals(enabled.bundleRevision, changed.bundleRevision)
  }
}
