package de.wea200675.lia.core

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Base64
import java.util.Locale

/** Explicitly confirmed, source-bound knowledge; never stores raw questions. */
data class KnowledgeMaintenanceReport(val keptEntries: Int, val serializedBytes: Int, val maxEntries: Int, val maxBytes: Int)

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
        val labels = sanitizeLabels(sourceLabels)
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

    fun all(): List<ConfirmedKnowledge> = prune(loadInternal(clock()), clock())\n\n    /** Explicit, idempotent cleanup hook for admin maintenance and diagnostics. */
    fun maintain(): KnowledgeMaintenanceReport {
        val now = clock()
        val kept = prune(loadInternal(now), now)
        persist(kept)
        return KnowledgeMaintenanceReport(kept.size, serialize(kept).size, maxEntries, maxBytes)
    }
    fun clear() = store.delete(key)

    private fun loadInternal(now: Long): List<ConfirmedKnowledge> {
        // Keystore authentication/tamper failures must propagate; treating them as an empty store would hide corruption.
        val raw = store.get(key) ?: return emptyList()
        var discarded = false
        val decoded = String(raw, StandardCharsets.UTF_8)
        val parsed = decoded.lineSequence().filter { it.isNotBlank() }.mapNotNull { line ->
            val item = runCatching { decode(line) }.getOrNull()
            val valid = item?.let(::validateStored)
            if (valid == null) discarded = true
            valid
        }.filter { item ->
            val active = item.expiresAtEpochMs > now
            if (!active) discarded = true
            active
        }.toList()
        if (discarded) persist(prune(parsed, now))
        return parsed
    }

    private fun decode(line: String): ConfirmedKnowledge {
        val p = line.split("|")
        require(p.size == 7)
        return ConfirmedKnowledge(
            fingerprint = p[0],
            summary = dec(p[1]),
            sourceLabels = p[2].split(",").filter(String::isNotEmpty).map(::dec),
            createdAtEpochMs = p[3].toLong(),
            lastUsedAtEpochMs = p[4].toLong(),
            useCount = p[5].toInt(),
            expiresAtEpochMs = p[6].toLong()
        )
    }

    private fun validateStored(item: ConfirmedKnowledge): ConfirmedKnowledge? {
        if (!item.fingerprint.matches(HEX)) return null
        val summary = UntrustedKnowledgeBoundary.sanitize(item.summary) ?: return null
        val labels = sanitizeLabels(item.sourceLabels)
        if (summary != item.summary || labels.size != item.sourceLabels.size || labels.isEmpty()) return null
        if (item.createdAtEpochMs < 0L || item.lastUsedAtEpochMs < item.createdAtEpochMs) return null
        if (item.expiresAtEpochMs <= item.createdAtEpochMs || item.useCount < 0) return null
        return item.copy(sourceLabels = labels)
    }

    private fun sanitizeLabels(labels: List<String>): List<String> = labels.asSequence()
        .map(String::trim)
        .filter { it.length in 1..120 }
        .filter { label -> label.none { it.isISOControl() || it in BIDI_CONTROLS } }
        .distinct()
        .take(5)
        .toList()

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
        private val BIDI_CONTROLS = setOf('\u202A', '\u202B', '\u202C', '\u202D', '\u202E', '\u2066', '\u2067', '\u2068', '\u2069')
        const val DEFAULT_TTL_MS = 180L * 24 * 60 * 60 * 1000
        const val MIN_TTL_MS = 60 * 60 * 1000L
        const val MAX_TTL_MS = 365L * 24 * 60 * 60 * 1000
        fun fingerprint(anonymizedQuestion: String): String = hash(canonicalize(anonymizedQuestion))

        /** New canonical key first, followed by the legacy exact key for existing installations. */
        fun fingerprintCandidates(anonymizedQuestion: String): List<String> =
            listOf(fingerprint(anonymizedQuestion), hash(anonymizedQuestion.trim())).distinct()

        internal fun canonicalize(question: String): String = Normalizer
            .normalize(question, Normalizer.Form.NFC)
            .lowercase(Locale.ROOT)
            .replace(Regex("""[\p{P}\p{S}]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
