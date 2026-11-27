package com.neonyne.lasthavenlauncher

/**
 * Pure wire contract for the addon-bridge native messages (issue #167).
 *
 * Requests and responses are flat JSON objects with scalar values only; the Android adapter is
 * responsible for converting to/from `org.json` and must reject nested structures. This layer owns
 * the schema: unknown fields, unknown types and out-of-bounds values fail closed.
 */
object AddonBridgeProtocol {
  const val KEY_TYPE = "type"
  const val KEY_REQUEST_ID = "requestId"
  const val KEY_ERROR = "error"
  const val KEY_REVISION = "revision"
  const val KEY_ADDONS = "addons"
  const val KEY_LOAD_ORDER = "loadOrder"
  const val KEY_BLOCKED = "blocked"
  const val KEY_FOLDER = "folder"
  const val KEY_VERSION = "version"
  const val KEY_ENABLED = "enabled"

  const val MAX_REQUEST_ID_CHARS = 64
  const val MAX_MESSAGE_FIELDS = 6
  const val MAX_RESPONSE_ITEMS = 256
  const val MAX_ITEM_CHARS = 128
  const val MAX_ERROR_CODE_CHARS = 32

  val MESSAGE_TYPES = setOf("ping", "listAddons", "getLoadOrder")
  val ERROR_CODES = setOf("invalidRequest", "unsupported", "tooManyAddons", "unavailable")

  private val REQUEST_ID_PATTERN = Regex("[A-Za-z0-9_-]+")
  private val ITEM_PATTERN = Regex("[A-Za-z0-9._-]+")

  data class AddonBridgeRequest(val type: String, val requestId: String)

  data class AddonSummary(val folder: String, val version: String, val enabled: Boolean)

  sealed interface AddonBridgeResponse {
    val requestId: String

    fun toFields(): Map<String, Any?>
  }

  data class AckResponse(override val requestId: String) : AddonBridgeResponse {
    override fun toFields(): Map<String, Any?> =
      mapOf(KEY_TYPE to "ack", KEY_REQUEST_ID to requestId)
  }

  data class ErrorResponse(override val requestId: String, val code: String) : AddonBridgeResponse {
    override fun toFields(): Map<String, Any?> =
      mapOf(KEY_TYPE to "error", KEY_REQUEST_ID to requestId, KEY_ERROR to code)
  }

  data class AddonListResponse(
    override val requestId: String,
    val revision: String,
    val addons: List<AddonSummary>,
  ) : AddonBridgeResponse {
    override fun toFields(): Map<String, Any?> =
      mapOf(
        KEY_TYPE to "listAddons",
        KEY_REQUEST_ID to requestId,
        KEY_REVISION to revision,
        KEY_ADDONS to
          addons.map {
            mapOf(
              KEY_FOLDER to it.folder,
              KEY_VERSION to it.version,
              KEY_ENABLED to it.enabled,
            )
          },
      )
  }

  data class LoadOrderResponse(
    override val requestId: String,
    val revision: String,
    val loadOrder: List<String>,
    val blocked: List<String>,
  ) : AddonBridgeResponse {
    override fun toFields(): Map<String, Any?> =
      mapOf(
        KEY_TYPE to "getLoadOrder",
        KEY_REQUEST_ID to requestId,
        KEY_REVISION to revision,
        KEY_LOAD_ORDER to loadOrder,
        KEY_BLOCKED to blocked,
      )
  }

  /** Parses an already-flattened message; returns null for any schema violation. */
  fun parse(fields: Map<String, Any?>): AddonBridgeRequest? {
    if (fields.size > MAX_MESSAGE_FIELDS) return null
    if (fields.keys.toSet() != setOf(KEY_TYPE, KEY_REQUEST_ID)) return null
    val type = fields[KEY_TYPE] as? String ?: return null
    if (type !in MESSAGE_TYPES) return null
    val requestId = fields[KEY_REQUEST_ID] as? String ?: return null
    if (!isRequestIdValid(requestId)) return null
    return AddonBridgeRequest(type, requestId)
  }

  fun ack(requestId: String): AckResponse? =
    if (isRequestIdValid(requestId)) AckResponse(requestId) else null

  fun error(requestId: String, code: String): ErrorResponse? {
    if (!isRequestIdValid(requestId)) return null
    if (code.length !in 1..MAX_ERROR_CODE_CHARS || code !in ERROR_CODES) return null
    return ErrorResponse(requestId, code)
  }

  fun addonList(
    requestId: String,
    revision: String,
    addons: List<AddonSummary>,
  ): AddonListResponse? {
    if (!isRequestIdValid(requestId) || !isRevisionValid(revision)) return null
    if (addons.size > MAX_RESPONSE_ITEMS) return null
    if (addons.any { !isItemText(it.folder) || !isItemText(it.version) }) return null
    return AddonListResponse(requestId, revision, addons)
  }

  fun loadOrder(
    requestId: String,
    revision: String,
    loadOrder: List<String>,
    blocked: List<String>,
  ): LoadOrderResponse? {
    if (!isRequestIdValid(requestId) || !isRevisionValid(revision)) return null
    if (loadOrder.size > MAX_RESPONSE_ITEMS || blocked.size > MAX_RESPONSE_ITEMS) return null
    if ((loadOrder + blocked).any { !isItemText(it) || it.length > MAX_ITEM_CHARS }) return null
    return LoadOrderResponse(requestId, revision, loadOrder, blocked)
  }

  fun isRequestIdValid(requestId: String): Boolean =
    requestId.length in 1..MAX_REQUEST_ID_CHARS && requestId.matches(REQUEST_ID_PATTERN)

  private fun isRevisionValid(revision: String): Boolean =
    revision.length in 1..MAX_ITEM_CHARS && revision.matches(ITEM_PATTERN)

  private fun isItemText(value: String): Boolean =
    value.length in 1..MAX_ITEM_CHARS && value.matches(ITEM_PATTERN)
}
