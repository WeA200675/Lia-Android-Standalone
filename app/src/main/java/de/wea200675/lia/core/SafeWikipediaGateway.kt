package de.wea200675.lia.core

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Bounded read-only Wikimedia cascade. The historical class name is retained
 * for source compatibility; all reachable hosts come from the audited catalog.
 */
class SafeWikipediaGateway(
    private val enabled: () -> Boolean,
    private val cache: BoundedKnowledgeCache = BoundedKnowledgeCache(),
    private val sourceHealth: KnowledgeSourceHealthTracker = KnowledgeSourceHealthTracker(),
    private val clock: () -> Long = System::currentTimeMillis
) : ProvenanceWebGateway {
    override suspend fun queryWithProvenance(anonymizedQuery: String): Result<KnowledgeBundle> = withContext(Dispatchers.IO) {
        runCatching {
            check(enabled()) { "Internet knowledge is disabled" }
            val approved = requireNotNull(OutboundQueryPolicy.approved(anonymizedQuery)) {
                "Query is not eligible for anonymous Internet lookup"
            }
            val cached = cache.get(approved)
            if (cached != null) {
                KnowledgeBundle(
                    text = cached,
                    provenance = KnowledgeProvenance(
                        sourceLabels = KnowledgeSourceLabels.fromRenderedContext(cached),
                        origin = KnowledgeOrigin.SESSION_CACHE,
                        retrievedAtEpochMs = clock()
                    )
                )
            } else {
                val results = KnowledgeSourceCatalog.select(approved)
                    .filter { sourceHealth.canAttempt(it.id) }
                    .mapNotNull { source ->
                        runCatching { source to querySource(source, approved) }
                            .onSuccess { sourceHealth.recordSuccess(source.id) }
                            .onFailure { sourceHealth.recordFailure(source.id) }
                            .getOrNull()
                    }
                    .take(MAX_RESULTS)
                check(results.isNotEmpty()) { "No curated knowledge result" }
                val rendered = results.joinToString("\n\n") { it.second }
                cache.put(approved, rendered, KnowledgeFreshnessPolicy.ttlMillis(approved))
                KnowledgeBundle(
                    text = rendered,
                    provenance = KnowledgeProvenance(
                        sourceLabels = results.map { it.first.label }.distinct(),
                        origin = KnowledgeOrigin.LIVE,
                        retrievedAtEpochMs = clock()
                    )
                )
            }
        }
    }

    private fun querySource(source: KnowledgeSource, query: String): String {
        require(KnowledgeSourceCatalog.isAllowed(source.host))
        val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
        val endpoint = URL(
            "https://${source.host}/w/api.php?action=query&generator=search&utf8=1&format=json&gsrlimit=1&gsrsearch=$encoded&prop=extracts&exintro=1&explaintext=1"
        )
        require(endpoint.protocol == "https" && KnowledgeSourceCatalog.isAllowed(endpoint.host))
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = false
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Lia-Android-Standalone/0.1 (local knowledge assistant)")
        }
        return try {
            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Knowledge source unavailable"
            }
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                while (output.size() <= MAX_RESPONSE_BYTES) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                }
                check(output.size() <= MAX_RESPONSE_BYTES) { "Knowledge response too large" }
                output.toByteArray()
            }
            val pages = JSONObject(String(bytes, Charsets.UTF_8))
                .optJSONObject("query")
                ?.optJSONObject("pages")
                ?: error("No result")
            val first = pages.keys().asSequence()
                .map { pages.getJSONObject(it) }
                .firstOrNull()
                ?: error("No result")
            val title = first.optString("title").trim().take(160)
            val snippet = first.optString("extract").trim().take(MAX_SNIPPET_CHARS)
            check(title.isNotEmpty() && snippet.isNotEmpty()) { "Empty knowledge result" }
            "[${source.label}] $title: $snippet"
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val MAX_RESULTS = 2
        const val MAX_RESPONSE_BYTES = 128 * 1024
        const val MAX_SNIPPET_CHARS = 1200
        const val CONNECT_TIMEOUT_MS = 2000
        const val READ_TIMEOUT_MS = 2500
    }
}
