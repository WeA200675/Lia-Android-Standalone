package de.wea200675.lia.core

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Narrow, read-only knowledge gateway. It can contact only German Wikipedia
 * over HTTPS, never follows redirects, and rejects personal-looking queries.
 */
class SafeWikipediaGateway(
    private val enabled: () -> Boolean
) : WebGateway {
    override suspend fun query(anonymizedQuery: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            check(enabled()) { "Internet knowledge is disabled" }
            val approved = requireNotNull(OutboundQueryPolicy.approved(anonymizedQuery)) {
                "Query is not eligible for anonymous Internet lookup"
            }
            val encoded = URLEncoder.encode(approved, Charsets.UTF_8.name())
            val endpoint = URL(
                "https://de.wikipedia.org/w/api.php?action=query&list=search&utf8=1&format=json&srlimit=1&srprop=snippet&srsearch=$encoded"
            )
            require(endpoint.protocol == "https" && endpoint.host == "de.wikipedia.org")
            val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = 3500
                readTimeout = 4500
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Lia-Android-Standalone/0.1 (local knowledge assistant)")
            }
            try {
                check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                    "Knowledge service unavailable"
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
                val first = JSONObject(String(bytes, Charsets.UTF_8))
                    .getJSONObject("query")
                    .getJSONArray("search")
                    .optJSONObject(0)
                    ?: error("No reviewed knowledge result")
                val title = first.optString("title").trim().take(160)
                val snippet = first.optString("snippet")
                    .replace(Regex("<[^>]+>"), "")
                    .replace("&quot;", "\"")
                    .replace("&#039;", "'")
                    .replace("&amp;", "&")
                    .trim()
                    .take(1800)
                check(title.isNotEmpty() && snippet.isNotEmpty()) { "Empty knowledge result" }
                "$title: $snippet"
            } finally {
                connection.disconnect()
            }
        }
    }

    private companion object {
        const val MAX_RESPONSE_BYTES = 128 * 1024
    }
}
