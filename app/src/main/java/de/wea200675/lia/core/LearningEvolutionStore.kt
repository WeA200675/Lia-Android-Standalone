package de.wea200675.lia.core

class LearningEvolutionStore(
    private val secureStore: SecureStore,
    private val key: String = "lia.learning.evolution.v1"
) {
    fun save(state: LearningEvolutionState) {
        val raw = listOf(
            state.stage.name,
            state.acceptedSignals,
            state.rejectedSignals,
            state.focusAreas.joinToString(",") { it.replace("|", " ") }
        ).joinToString("|")
        secureStore.put(key, raw.toByteArray(Charsets.UTF_8))
    }

    fun load(): LearningEvolutionState {
        val raw = secureStore.get(key)?.toString(Charsets.UTF_8) ?: return initial()
        val parts = raw.split("|")
        val stage = runCatching { DevelopmentStage.valueOf(parts.getOrNull(0).orEmpty()) }
            .getOrDefault(DevelopmentStage.FOUNDATION)
        val accepted = parts.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val rejected = parts.getOrNull(2)?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val focus = parts.getOrNull(3).orEmpty().split(",").filter { it.isNotBlank() }.take(8).toSet()
        return LearningEvolutionState(stage, accepted, rejected, focus)
    }

    fun reset() {
        secureStore.delete(key)
    }

    private fun initial() = LearningEvolutionState(DevelopmentStage.FOUNDATION, 0, 0, emptySet())
}
