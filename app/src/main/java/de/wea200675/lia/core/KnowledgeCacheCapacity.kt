package de.wea200675.lia.core

data class KnowledgeCachePlan(
    val maxEntries: Int,
    val estimatedMaxKilobytes: Int,
    val profile: String
)

object KnowledgeCacheCapacity {
    /**
     * Capacity scales with physical memory and source breadth, but always stays
     * bounded. Storage expansion does not silently increase RAM consumption.
     */
    fun recommend(ramMb: Long, sourceCount: Int): KnowledgeCachePlan {
        val memoryCeiling = when {
            ramMb < 4_096 -> 24
            ramMb < 6_144 -> 48
            ramMb < 8_192 -> 96
            ramMb < 12_288 -> 160
            else -> 256
        }
        val usefulForSources = (sourceCount.coerceAtLeast(1) * 6).coerceAtLeast(24)
        val entries = usefulForSources.coerceAtMost(memoryCeiling)
        val profile = when {
            entries <= 24 -> "sparsam"
            entries <= 96 -> "ausgewogen"
            entries <= 160 -> "erweitert"
            else -> "maximal-begrenzt"
        }
        return KnowledgeCachePlan(
            maxEntries = entries,
            estimatedMaxKilobytes = entries * 4,
            profile = profile
        )
    }
}
