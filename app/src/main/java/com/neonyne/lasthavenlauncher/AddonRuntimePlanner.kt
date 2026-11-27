package com.neonyne.lasthavenlauncher

import java.security.MessageDigest
import java.util.Locale

sealed class AddonBlockedReason {
  data class MissingDependencies(val missing: List<String>) : AddonBlockedReason()

  data object DependencyCycle : AddonBlockedReason()
}

data class AddonLoadPlan(
  val sourceRevision: String,
  val orderedAddons: List<AddonLibraryItem>,
  val blockedAddons: Map<String, AddonBlockedReason>,
  val bundleRevision: String,
) {
  val isEmpty: Boolean
    get() = orderedAddons.isEmpty()
}

/**
 * Pure runtime planner. It never enables addons or touches the library; it only creates a stable
 * load order for the currently enabled snapshot and reports addons that cannot safely load.
 */
object AddonRuntimePlanner {
  /**
   * Input contract: snapshots must come from the validated local library. Enforce the same
   * archive limits here as a defensive boundary before a future runtime consumes the plan.
   */
  fun plan(snapshot: AddonLibrarySnapshot): AddonLoadPlan {
    require(snapshot.addons.size <= AddonArchive.MAX_ADDONS_PER_ARCHIVE) {
      "Addon snapshot exceeds the validated library item limit."
    }
    snapshot.addons.forEach { addon ->
      require(addon.dependencies.size <= AddonArchive.MAX_DEPENDENCIES) {
        "Addon dependency list exceeds the validated limit."
      }
      require(addon.optionalDependencies.size <= AddonArchive.MAX_DEPENDENCIES) {
        "Addon optional dependency list exceeds the validated limit."
      }
      require(
        (addon.dependencies + addon.optionalDependencies).all {
          it.length <= AddonArchive.MAX_DEPENDENCY_NAME_CHARS &&
            it.matches(Regex("[A-Za-z0-9_-]+"))
        }
      ) {
        "Addon contains an invalid dependency name."
      }
    }
    val enabled = snapshot.addons.filter { it.enabled }
    if (enabled.isEmpty()) {
      return AddonLoadPlan(
        sourceRevision = snapshot.revision,
        orderedAddons = emptyList(),
        blockedAddons = emptyMap(),
        bundleRevision = sha256("empty:${snapshot.revision}"),
      )
    }

    val report = AddonDependencyAnalyzer.analyze(snapshot.addons)
    val enabledByLower = enabled.associateBy { it.folder.lowercase(Locale.ROOT) }
    val blocked = linkedMapOf<String, AddonBlockedReason>()

    for (addon in enabled) {
      if (addon.folder in report.cyclicFolders) {
        blocked[addon.folder] = AddonBlockedReason.DependencyCycle
      }
    }

    var changed = true
    while (changed) {
      changed = false
      for (addon in enabled) {
        if (addon.folder in blocked) continue
        val unavailable =
          addon.dependencies.filter { dependency ->
            val target = enabledByLower[dependency.lowercase(Locale.ROOT)]
            target == null || target.folder in blocked
          }
        if (unavailable.isNotEmpty()) {
          blocked[addon.folder] = AddonBlockedReason.MissingDependencies(unavailable)
          changed = true
        }
      }
    }

    val loadable = enabled.filter { it.folder !in blocked }
    val byLower = loadable.associateBy { it.folder.lowercase(Locale.ROOT) }
    val inDegree = byLower.keys.associateWith { 0 }.toMutableMap()
    val dependents = byLower.keys.associateWith { mutableListOf<String>() }

    for (addon in loadable) {
      val node = addon.folder.lowercase(Locale.ROOT)
      val dependencies =
        addon.dependencies.map { it.lowercase(Locale.ROOT) }.filter { it in byLower }.distinct()
      for (dependency in dependencies) {
        dependents.getValue(dependency) += node
        inDegree[node] = inDegree.getValue(node) + 1
      }
    }

    val ready = inDegree.filterValues { it == 0 }.keys.toMutableSet()
    val emitted = mutableSetOf<String>()
    val ordered = mutableListOf<AddonLibraryItem>()
    while (ready.isNotEmpty()) {
      val next =
        ready.minWith(
          compareBy<String>(
            { key ->
              byLower.getValue(key).optionalDependencies.count { optional ->
                val lower = optional.lowercase(Locale.ROOT)
                lower in byLower && lower !in emitted
              }
            },
            { it },
          )
        )
      ready.remove(next)
      emitted += next
      ordered += byLower.getValue(next)
      for (dependent in dependents.getValue(next)) {
        val degree = inDegree.getValue(dependent) - 1
        inDegree[dependent] = degree
        if (degree == 0) ready += dependent
      }
    }

    val signature = buildString {
      append(snapshot.revision)
      ordered.forEach { append('|').append(it.folder).append(':').append(it.importedAt) }
      blocked.forEach { (folder, reason) -> append("!").append(folder).append(':').append(reason) }
    }
    return AddonLoadPlan(
      sourceRevision = snapshot.revision,
      orderedAddons = ordered,
      blockedAddons = blocked,
      bundleRevision = sha256(signature),
    )
  }

  /** Returns safe, unique relative paths from TOC load-order lines; comments are ignored. */
  fun parseTocLoadFiles(tocContent: String): List<String> {
    require(tocContent.toByteArray(Charsets.UTF_8).size <= AddonArchive.MAX_TOC_BYTES) {
      "Addon TOC exceeds the 1 MiB limit."
    }
    val files = mutableListOf<String>()
    val seen = mutableSetOf<String>()
    for (rawLine in tocContent.lineSequence()) {
      val line = rawLine.trim().trimStart('\uFEFF')
      if (line.isEmpty() || line.startsWith("#")) continue
      val path = runCatching { AddonArchive.validateEntryPath(line) }.getOrNull() ?: continue
      if (seen.add(path.lowercase(Locale.ROOT))) {
        require(files.size < AddonArchive.MAX_ENTRIES) { "Addon TOC contains too many load entries." }
        files += path
      }
    }
    return files
  }

  private fun sha256(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString(
      ""
    ) {
      "%02x".format(it)
    }
}
