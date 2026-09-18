package de.wea200675.lia.core

private const val OFFLINE_MESSAGE =
    "Die Websuche ist momentan nicht verfügbar. Lokaler Offline-Modus aktiv."

/** Small, reviewed offline knowledge set; it never receives or stores personal data. */
object OfflineKnowledgeFallback : WebGateway {
    private val answers = mapOf(
        "wie entsteht ein regenbogen?" to
            "Ein Regenbogen entsteht, wenn Sonnenlicht in Regentropfen gebrochen, gespiegelt und in Farben aufgefächert wird."
    )

    override suspend fun query(anonymizedQuery: String): Result<String> {
        val answer = answers[anonymizedQuery.trim().lowercase()]
            ?: return Result.failure(IllegalArgumentException("Kein lokaler Wissenseintrag"))
        return Result.success(answer)
    }
}

/** Deterministic local response used when no reviewed offline answer is available. */
object OfflineWebFallback : WebGateway {
    override suspend fun query(anonymizedQuery: String): Result<String> =
        Result.success(
            "Ich kann diese Wissensfrage gerade offline nicht nachschlagen. " +
                "Ich bleibe trotzdem sicher und nutze keine unbereinigten Daten."
        )
}

/**
 * A bounded cascade: primary gateway, one configured local fallback, then the
 * reviewed offline knowledge set. No stage retries a failed network request.
 */
class ResilientWebGateway(
    private val primary: WebGateway,
    private val fallback: WebGateway = OfflineWebFallback,
    private val offlineKnowledge: WebGateway = OfflineKnowledgeFallback
) : WebGateway {
    override suspend fun query(anonymizedQuery: String): Result<String> {
        val primaryResult = primary.query(anonymizedQuery)
        if (primaryResult.isSuccess) return primaryResult

        val fallbackResult = fallback.query(anonymizedQuery)
        if (fallbackResult.isSuccess) return fallbackResult

        val knowledgeResult = offlineKnowledge.query(anonymizedQuery)
        return knowledgeResult.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.success(OFFLINE_MESSAGE) }
        )
    }
}
