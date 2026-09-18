package de.wea200675.lia.core

import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Explicitly confirmed, source-bound knowledge; never stores raw questions. */
data class ConfirmedKnowledge(
    val fingerprint: String,
    val summary: String,
    val sourceLabels: List<String>,
    val createdAtEpochMs: Long,
    val lastUsedAtEpochMs: Long,
    val useCount: Int,
    val expiresAtEpochMs: Long
)

class ConfirmedKnowledgeRepository(
    private val store: SecureStore,
    private val key: String = "lia.confirmed.knowledge.v1",
    private val maxEntries: Int = 128,
    private val maxBytes: Int = 256 * 1024,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    init {
        require(maxEntries in 1..512)
        require(maxBytes in 4096..2 * 1024 * 1024)
    }

    fun saveConfirmed(
        sourceBoundSummary: String,
        sourceLabels: List<String>,
        questionFingerprint: String,
        ttlMs: Long = DEFAULT_TTL_MS
    ): Boolean {
        val summary = UntrustedKnowledgeBoundary.sanitize(sourceBoundSummary) ?: return false
        val labels = sourceLabels.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(5)
        if (labels.isEmpty() || !questionFingerprint.matches(Regex("[a-f0-9]{64}")) || ttlMs !in MIN_TTL_MS..MAX_TTL_MS) return false
        val now = clock()
        val current = loadInternal(now).toMutableList()
        val item = ConfirmedKnowledge(questionFingerprint, summary, labels, now, now, 0, now + ttlMs)
        current.removeAll { it.fingerprint == item.fingerprint }
        current += item
        persist(prune(current, now))
        return true
    }

    fun find(questionFingerprint: String): ConfirmedKnowledge? {
        if (!questionFingerprint.matches(Regex("[a-f0-9]{64}"))) return null
        val now = clock()
        val items = loadInternal(now).toMutableList()
        val found = items.firstOrNull { it.fingerprint == questionFingerprint } ?: return null
        items.remove(found)
        val touched = found.copy(lastUsedAtEpochMs = now, useCount = found.useCount + 1)
        items += touched
        persist(prune(items, now))
        return touched
    }

    fun all(): List<ConfirmedKnowledge> = prune(loadInternal(clock()), clock())
    fun clear() = store.delete(key)

    private fun loadInternal(now: Long): List<ConfirmedKnowledge> {
        val raw = store.get(key) ?: return emptyList()
        return try {
            val array = JSONArray(String(raw, StandardCharsets.UTF_8))
            (0 until array.length()).mapNotNull { index ->
                val o = array.optJSONObject(index) ?: return@mapNotNull null
                val item = ConfirmedKnowledge(
                    o.optString("fingerprint"), o.optString("summary"),
                    (0 until o.optJSONArray("sources").length()).map { o.optJSONArray("sources").optString(it) },
                    o.optLong("created"), o.optLong("used"), o.optInt("uses"), o.optLong("expires")
                )
                if (item.fingerprint.matches(Regex("[a-f0-9]{64}")) &&
                    item.summary.isNotBlank() && item.sourceLabels.isNotEmpty() && item.expiresAtEpochMs > now) item else null
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun persist(items: List<ConfirmedKnowledge>) {
        if (items.isEmpty()) { store.delete(key); return }
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().put("fingerprint", item.fingerprint).put("summary", item.summary)
                .put("sources", JSONArray(item.sourceLabels)).put("created", item.createdAtEpochMs)
                .put("used", item.lastUsedAtEpochMs).put("uses", item.useCount).put("expires", item.expiresAtEpochMs))
        }
        store.put(key, array.toString().toByteArray(StandardCharsets.UTF_8))
    }

    private fun prune(items: List<ConfirmedKnowledge>, now: Long): List<ConfirmedKnowledge> {
        val valid = items.filter { it.expiresAtEpochMs > now }
            .distinctBy { it.fingerprint }
            .sortedWith(compareByDescending<ConfirmedKnowledge> { it.useCount }.thenByDescending { it.lastUsedAtEpochMs })
            .take(maxEntries)
        var result = valid
        while (result.isNotEmpty() && serialize(result).size > maxBytes) result = result.dropLast(1)
        return result
    }

    private fun serialize(items: List<ConfirmedKnowledge>): ByteArray {
        val a = JSONArray(); items.forEach { a.put(JSONObject().put("fingerprint", it.fingerprint).put("summary", it.summary).put("sources", JSONArray(it.sourceLabels)).put("created", it.createdAtEpochMs).put("used", it.lastUsedAtEpochMs).put("uses", it.useCount).put("expires", it.expiresAtEpochMs)) }
        return a.toString().toByteArray(StandardCharsets.UTF_8)
    }

    companion object {
        const val DEFAULT_TTL_MS = 180L * 24 * 60 * 60 * 1000
        const val MIN_TTL_MS = 60 * 60 * 1000L
        const val MAX_TTL_MS = 365L * 24 * 60 * 60 * 1000
        fun fingerprint(anonymizedQuestion: String): String =
            MessageDigest.getInstance("SHA-256").digest(anonymizedQuestion.trim().toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
    }
}
