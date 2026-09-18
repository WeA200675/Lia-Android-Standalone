package de.wea200675.lia.core

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

/** Explicitly confirmed, source-bound knowledge; never stores raw questions. */
data class ConfirmedKnowledge(
    val fingerprint: String, val summary: String, val sourceLabels: List<String>,
    val createdAtEpochMs: Long, val lastUsedAtEpochMs: Long, val useCount: Int, val expiresAtEpochMs: Long
)

class ConfirmedKnowledgeRepository(
    private val store: SecureStore, private val key: String = "lia.confirmed.knowledge.v1",
    private val maxEntries: Int = 128, private val maxBytes: Int = 256 * 1024,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    init { require(maxEntries in 1..512); require(maxBytes in 4096..2 * 1024 * 1024) }

    fun saveConfirmed(sourceBoundSummary: String, sourceLabels: List<String>, questionFingerprint: String, ttlMs: Long = DEFAULT_TTL_MS): Boolean {
        val summary = (UntrustedKnowledgeBoundary.sanitize(sourceBoundSummary) ?: return false)
            .replace(Regex("""^\[[^]]{1,120}]\s*"""), "")
            .trim()
        if (summary.length < 3) return false
        val labels = sourceLabels.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(5)
        if (labels.isEmpty() || !questionFingerprint.matches(HEX) || ttlMs !in MIN_TTL_MS..MAX_TTL_MS) return false
        val now = clock(); val current = loadInternal(now).toMutableList()
        current.removeAll { it.fingerprint == questionFingerprint }
        current += ConfirmedKnowledge(questionFingerprint, summary, labels, now, now, 0, now + ttlMs)
        persist(prune(current, now)); return true
    }

    fun find(questionFingerprint: String): ConfirmedKnowledge? {
        if (!questionFingerprint.matches(HEX)) return null
        val now = clock(); val items = loadInternal(now).toMutableList()
        val found = items.firstOrNull { it.fingerprint == questionFingerprint } ?: return null
        items.remove(found); val touched = found.copy(lastUsedAtEpochMs = now, useCount = found.useCount + 1)
        items += touched; persist(prune(items, now)); return touched
    }

    fun all(): List<ConfirmedKnowledge> = prune(loadInternal(clock()), clock())
    fun clear() = store.delete(key)

    private fun loadInternal(now: Long): List<ConfirmedKnowledge> = try {
        val text = store.get(key)?.toString(StandardCharsets.UTF_8) ?: return emptyList()
        text.lineSequence().mapNotNull { line ->
            val p = line.split("|"); if (p.size != 7) return@mapNotNull null
            val item = ConfirmedKnowledge(p[0], dec(p[1]), p[2].split(",").filter { it.isNotEmpty() }.map(::dec), p[3].toLong(), p[4].toLong(), p[5].toInt(), p[6].toLong())
            if (item.fingerprint.matches(HEX) && item.summary.isNotBlank() && item.sourceLabels.isNotEmpty() && item.expiresAtEpochMs > now) item else null
        }.toList()
    } catch (_: Exception) { emptyList() }

    private fun persist(items: List<ConfirmedKnowledge>) {
        if (items.isEmpty()) { store.delete(key); return }
        store.put(key, serialize(items))
    }

    private fun prune(items: List<ConfirmedKnowledge>, now: Long): List<ConfirmedKnowledge> {
        val valid = items.filter { it.expiresAtEpochMs > now }.distinctBy { it.fingerprint }
            .sortedWith(compareByDescending<ConfirmedKnowledge> { it.useCount }.thenByDescending { it.lastUsedAtEpochMs }).take(maxEntries)
        var result = valid
        while (result.isNotEmpty() && serialize(result).size > maxBytes) result = result.dropLast(1)
        return result
    }

    private fun serialize(items: List<ConfirmedKnowledge>): ByteArray = items.joinToString("\n") { i ->
        listOf(i.fingerprint, enc(i.summary), i.sourceLabels.joinToString(",") { enc(it) }, i.createdAtEpochMs, i.lastUsedAtEpochMs, i.useCount, i.expiresAtEpochMs).joinToString("|")
    }.toByteArray(StandardCharsets.UTF_8)

    private fun enc(value: String) = Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
    private fun dec(value: String) = String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8)

    companion object {
        private val HEX = Regex("[a-f0-9]{64}")
        const val DEFAULT_TTL_MS = 180L * 24 * 60 * 60 * 1000
        const val MIN_TTL_MS = 60 * 60 * 1000L
        const val MAX_TTL_MS = 365L * 24 * 60 * 60 * 1000
        fun fingerprint(anonymizedQuestion: String): String = MessageDigest.getInstance("SHA-256")
            .digest(anonymizedQuestion.trim().toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
