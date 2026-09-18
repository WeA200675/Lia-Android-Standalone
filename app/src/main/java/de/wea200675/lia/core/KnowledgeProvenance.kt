package de.wea200675.lia.core

enum class KnowledgeOrigin { LIVE, SESSION_CACHE, CONFIRMED_STORE }

data class KnowledgeProvenance(
    val sourceLabels: List<String>,
    val origin: KnowledgeOrigin,
    val retrievedAtEpochMs: Long
)

data class KnowledgeBundle(
    val text: String,
    val provenance: KnowledgeProvenance
)

/** Optional richer contract; legacy gateways remain compatible through WebGateway. */
interface ProvenanceWebGateway : WebGateway {
    suspend fun queryWithProvenance(anonymizedQuery: String): Result<KnowledgeBundle>

    override suspend fun query(anonymizedQuery: String): Result<String> =
        queryWithProvenance(anonymizedQuery).map { it.text }
}

object KnowledgeSourceLabels {
    private val label = Regex("""(?m)^\[([^]]{1,120})]\s*""")

    fun fromRenderedContext(text: String): List<String> =
        label.findAll(text).map { it.groupValues[1].trim() }.filter { it.isNotEmpty() }.distinct().take(3).toList()
}
