package com.neonyne.lasthavenlauncher

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the bridge install lifecycle's fail-closed paths (issue #167): every installation failure
 * must notify all queued callers, and port connections/messages must revalidate their sender.
 *
 * These are source-contract tests, not GeckoView runtime tests. Physical ARM64 verification remains
 * a separate acceptance requirement.
 */
class AddonBridgeInstallFailureTest {
  private val sourceRoot =
    File("src/main/java").takeIf { it.isDirectory } ?: File("app/src/main/java")

  private fun controllerSource(): String =
    File(sourceRoot, "com/neonyne/lasthavenlauncher/AddonBridgeController.kt").readText()

  @Test
  fun `every install failure path notifies all queued callers with false`() {
    val source = controllerSource()
    val ensureInstalled =
      source.substringAfter("fun ensureInstalled(").substringBefore("fun teardown")

    assertTrue(
      "runtime initialization failure must fail closed",
      ensureInstalled.contains(
        "\"Addon bridge runtime initialization failed: \${error.message}\""
      ) &&
        ensureInstalled
          .substringAfter("\"Addon bridge runtime initialization failed: \${error.message}\"")
          .substringBefore("val installation =")
          .contains("completeInstallation(false)"),
    )
    assertTrue(
      "ensureBuiltIn failure must fail closed",
      ensureInstalled.contains("\"Addon bridge extension install failed: \${error.message}\"") &&
        ensureInstalled
          .substringAfter("\"Addon bridge extension install failed: \${error.message}\"")
          .substringBefore("installation.accept(")
          .contains("completeInstallation(false)"),
    )
    val acceptBlock = ensureInstalled.substringAfter("installation.accept(")
    val rejectionBlock = acceptBlock.substringAfter("{ error ->")
    assertTrue(
      "rejected installation must fail closed",
      rejectionBlock.contains("completeInstallation(false)"),
    )
    assertTrue(
      "a null extension result must fail closed",
      acceptBlock.contains("extension == null") &&
        acceptBlock.contains("\"Addon bridge extension install returned no extension\""),
    )
    assertTrue(
      "delegate registration failure must fail closed",
      acceptBlock.contains(
        "\"Addon bridge message delegate registration failed: \${error.message}\""
      ),
    )

    val completion =
      source.substringAfter("private fun completeInstallation(").substringBefore("fun teardown")
    assertTrue(
      "installation completion must drain every queued callback",
      completion.contains("installationCallbacks.toList()") &&
        completion.contains("installationCallbacks.clear()") &&
        completion.contains("callback(installed)"),
    )
    assertTrue(
      "stale installation generations must be ignored",
      ensureInstalled.contains("if (generation != installationGeneration) return@post"),
    )
  }

  @Test
  fun `port connections and port messages revalidate the sender and fail closed`() {
    val source = controllerSource()

    val onConnect =
      source
        .substringAfter("override fun onConnect(port: WebExtension.Port)")
        .substringBefore("private val portDelegate")
    assertTrue(
      "onConnect must reject disallowed senders and port overflow",
      onConnect.contains("!senderAllowed(port.sender)") &&
        onConnect.contains("AddonBridgeTimeoutPolicy.canAcceptConnection(connectedPorts.size)"),
    )
    assertTrue(
      "rejected ports must be disconnected before any delegate is attached",
      onConnect.indexOf("port.disconnect()") >= 0 &&
        onConnect.indexOf("port.disconnect()") <
          onConnect.indexOf("port.setDelegate(portDelegate)"),
    )

    val portDelegate =
      source
        .substringAfter("object : WebExtension.PortDelegate")
        .substringBefore("private companion object")
    val onPortMessage = portDelegate.substringAfter("onPortMessage").substringBefore("onDisconnect")
    assertTrue(
      "port messages must revalidate the sender and disconnect on failure",
      onPortMessage.contains("!senderAllowed(port.sender)") &&
        onPortMessage.contains("port.disconnect()"),
    )
    val onDisconnect = portDelegate.substringAfter("onDisconnect")
    assertTrue(
      "port disconnect must drop the port and complete its abandoned pending requests",
      onDisconnect.contains("connectedPorts.remove(port)") &&
        onDisconnect.contains("completePendingRequest"),
    )
  }
}
