package com.neonyne.lasthavenlauncher

import java.util.ArrayDeque
import java.util.Locale

data class AddonDependencyReport(
  val missingByFolder: Map<String, List<String>>,
  val cyclicFolders: Set<String>,
)

/** Iterative dependency analysis; avoids recursion on untrusted addon metadata. */
object AddonDependencyAnalyzer {
  fun analyze(addons: List<AddonLibraryItem>): AddonDependencyReport {
    val byName = addons.associateBy { it.folder.lowercase(Locale.ROOT) }
    val enabledNames = addons.filter { it.enabled }.map { it.folder.lowercase(Locale.ROOT) }.toSet()
    val missing = addons.associate { addon ->
      addon.folder to
        addon.dependencies.filter { dependency ->
          dependency.lowercase(Locale.ROOT) !in enabledNames
        }
    }

    val graph = addons.associate { addon ->
      addon.folder.lowercase(Locale.ROOT) to
        addon.dependencies
          .mapNotNull { dependency ->
            dependency.lowercase(Locale.ROOT).takeIf(byName::containsKey)
          }
          .distinct()
    }
    val visited = mutableSetOf<String>()
    val finishOrder = mutableListOf<String>()

    for (start in graph.keys) {
      if (!visited.add(start)) continue
      val stack = ArrayDeque<Pair<String, Iterator<String>>>()
      stack.addLast(start to graph[start].orEmpty().iterator())
      while (stack.isNotEmpty()) {
        val frame = stack.peekLast() ?: continue
        val (node, edges) = frame
        if (edges.hasNext()) {
          val next = edges.next()
          if (visited.add(next)) {
            stack.addLast(next to graph[next].orEmpty().iterator())
          }
        } else {
          finishOrder += node
          stack.removeLast()
        }
      }
    }

    val reverse = graph.keys.associateWith { mutableListOf<String>() }
    graph.forEach { (node, dependencies) ->
      dependencies.forEach { dependency -> reverse.getValue(dependency) += node }
    }

    visited.clear()
    val cyclic = mutableSetOf<String>()
    for (start in finishOrder.asReversed()) {
      if (!visited.add(start)) continue
      val component = mutableListOf<String>()
      val stack = ArrayDeque<String>()
      stack.addLast(start)
      while (stack.isNotEmpty()) {
        val node = stack.removeLast()
        component += node
        reverse.getValue(node).forEach { next ->
          if (visited.add(next)) stack.addLast(next)
        }
      }
      if (component.size > 1 || graph[start].orEmpty().contains(start)) {
        cyclic += component
      }
    }

    val displayNames = addons.associate { it.folder.lowercase(Locale.ROOT) to it.folder }
    return AddonDependencyReport(
      missingByFolder = missing,
      cyclicFolders = cyclic.mapNotNullTo(mutableSetOf(), displayNames::get),
    )
  }
}
