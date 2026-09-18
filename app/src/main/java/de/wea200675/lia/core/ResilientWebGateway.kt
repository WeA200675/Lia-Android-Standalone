package de.wea200675.lia.core

/** Deterministic local response used when an approved web request cannot run safely. */
object OfflineWebFallback : WebGateway {
    override suspend fun query(anonymizedQuery: String): Result<String> =
        Result.success("Ich kann diese Wissensfrage gerade offline nicht nachschlagen. Ich bleibe trotzdem sicher und nutze keine unbereinigten Daten.")
}

/** Keeps web failures local: no retry loop and no unredacted fallback request. */
class ResilientWebGateway(
    private val primary: WebGateway,
    private val fallback: WebGateway = OfflineWebFallback
) : WebGateway {
    override suspend fun query(anonymizedQuery: String): Result<String> =
        primary.query(anonymizedQuery).getOrElse {
            Result.success(
                fallback.query(anonymizedQuery).getOrElse {
                    "Die Websuche ist momentan nicht verfügbar. Lokaler Offline-Modus aktiv."
                }
            )
        }
}
