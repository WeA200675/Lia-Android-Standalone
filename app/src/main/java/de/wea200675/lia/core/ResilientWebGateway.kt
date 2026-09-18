package de.wea200675.lia.core

private const val OFFLINE_MESSAGE =
    "Die Websuche ist momentan nicht verfügbar. Lokaler Offline-Modus aktiv."

/** Deterministic local response used when an approved web request cannot run safely. */
object OfflineWebFallback : WebGateway {
    override suspend fun query(anonymizedQuery: String): Result<String> =
        Result.success(
            "Ich kann diese Wissensfrage gerade offline nicht nachschlagen. " +
                "Ich bleibe trotzdem sicher und nutze keine unbereinigten Daten."
        )
}

/**
 * Keeps web failures local. A failure never triggers a retry with different data
 * and never bypasses the existing WebPolicy/Anonymizer gates.
 */
class ResilientWebGateway(
    private val primary: WebGateway,
    private val fallback: WebGateway = OfflineWebFallback
) : WebGateway {
    override suspend fun query(anonymizedQuery: String): Result<String> {
        val primaryResult = primary.query(anonymizedQuery)
        if (primaryResult.isSuccess) return primaryResult

        val fallbackResult = fallback.query(anonymizedQuery)
        return Result.success(fallbackResult.getOrElse { OFFLINE_MESSAGE })
    }
}
