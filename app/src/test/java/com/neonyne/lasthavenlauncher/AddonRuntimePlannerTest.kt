package com.neonyne.lasthavenlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Covers stable dependency ordering and the bounds expected at the future runtime boundary. */
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

    val enabled = AddonRuntimePlanner.plan(AddonLibrarySnapshot("r1", listOf(item("Addon"))))
    val changed = AddonRuntimePlanner.plan(AddonLibrarySnapshot("r2", listOf(item("Addon"))))
    assertNotEquals(enabled.bundleRevision, changed.bundleRevision)
  }

  @Test
  fun requiredDependenciesMatchFolderNamesCaseInsensitively() {
    val plan =
      AddonRuntimePlanner.plan(
        AddonLibrarySnapshot(
          "case-fold",
          listOf(item("BagSort", dependencies = listOf("aceCORE")), item("AceCore")),
        )
      )
    assertEquals(listOf("AceCore", "BagSort"), plan.orderedAddons.map { it.folder })
    assertTrue(plan.blockedAddons.isEmpty())
  }

  @Test
  fun optionalDependenciesPreferProviderBeforeConsumerWithoutBlocking() {
    val plan =
      AddonRuntimePlanner.plan(
        AddonLibrarySnapshot(
          "optional-order",
          listOf(
            item("OptionalConsumer", optionalDependencies = listOf("OptionalProvider")),
            item("OptionalProvider"),
            item("Independent"),
          ),
        )
      )
    val ordered = plan.orderedAddons.map { it.folder }
    assertTrue(ordered.indexOf("OptionalProvider") < ordered.indexOf("OptionalConsumer"))
    assertTrue(plan.blockedAddons.isEmpty())
  }

  @Test
  fun deepDependencyChainIsPlannedWithoutRecursiveTraversal() {
    val addons =
      (0 until 512).map { index ->
        item(
          "Addon$index",
          dependencies = if (index == 0) emptyList() else listOf("Addon${index - 1}"),
        )
      }
    val plan = AddonRuntimePlanner.plan(AddonLibrarySnapshot("deep-chain", addons))
    assertEquals(MAX_ADDON_LIBRARY_ITEMS, plan.orderedAddons.size)
    assertEquals("Addon0", plan.orderedAddons.first().folder)
    assertEquals(
      "Addon${MAX_ADDON_LIBRARY_ITEMS - 1}",
      plan.orderedAddons.last().folder,
    )
  }

  @Test
  fun plannerAcceptsMoreAddonsThanOneImportArchiveCanContain() {
    val addons = (0..AddonArchive.MAX_ADDONS_PER_ARCHIVE).map { item("Addon$it") }
    val plan = AddonRuntimePlanner.plan(AddonLibrarySnapshot("multi-import-library", addons))
    assertEquals(AddonArchive.MAX_ADDONS_PER_ARCHIVE + 1, plan.orderedAddons.size)
  }

  @Test
  fun plannerRejectsSnapshotsBeyondValidatedLibraryBounds() {
    val oversized = (0..MAX_ADDON_LIBRARY_ITEMS).map { item("Addon$it") }
    org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
      AddonRuntimePlanner.plan(AddonLibrarySnapshot("oversized", oversized))
    }
  }

  @Test
  fun tocLoadOrderRejectsOversizedInput() {
    org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
      AddonRuntimePlanner.parseTocLoadFiles("x".repeat(AddonArchive.MAX_TOC_BYTES.toInt() + 1))
    }
  }
}
