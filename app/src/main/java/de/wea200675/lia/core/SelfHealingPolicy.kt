package de.wea200675.lia.core

enum class RecoveryLevel { FULL, REDUCED, SAFE, BLOCKED }

data class RecoveryDecision(
    val level: RecoveryLevel,
    val threads: Int,
    val delayMillis: Long,
    val restartAllowed: Boolean
)

class SelfHealingPolicy(
    private val maxRestarts: Int = 3,
    private val physicalCores: Int,
    private val logicalThreads: Int
) {
    init { require(maxRestarts >= 0); require(physicalCores > 0); require(logicalThreads >= physicalCores) }

    fun decide(failures: Int): RecoveryDecision {
        if (failures >= maxRestarts) return RecoveryDecision(RecoveryLevel.BLOCKED, 0, 0, false)
        val logical = logicalThreads.coerceAtLeast(1)
        val physical = physicalCores.coerceAtLeast(1)
        return when (failures) {
            0 -> RecoveryDecision(RecoveryLevel.FULL, logical, 0, true)
            1 -> RecoveryDecision(RecoveryLevel.REDUCED, (logical * 0.75).toInt().coerceAtLeast(1), 2_000, true)
            else -> RecoveryDecision(RecoveryLevel.SAFE, physical, 8_000, true)
        }
    }
}
