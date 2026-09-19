package de.wea200675.lia.core

data class LearningFeedbackSignal(
    val text: String,
    val positive: Boolean,
    val kind: LearningSignalKind = LearningSignalKind.FEEDBACK
)

class AdaptiveLearningService(
    private val evolutionStore: LearningEvolutionStore,
    private val secureStore: SecureStore,
    private val historyKey: String = "lia.learning.feedback.v1"
) {
    fun record(signal: LearningFeedbackSignal): LearningEvolutionState {
        val normalized = normalize(signal.text)
        if (normalized.isEmpty() || normalized.length > LearningEvolution.MAX_SIGNAL_LENGTH) {
            return evolutionStore.load()
        }
        val previous = history()
        if (previous.contains(normalized)) return evolutionStore.load()
        val bounded = (previous + normalized).takeLast(MAX_HISTORY)
        secureStore.put(historyKey, bounded.joinToString("\n").toByteArray(Charsets.UTF_8))
        val next = LearningEvolution.accept(
            evolutionStore.load(),
            listOf(LearningSignal(signal.kind, normalized, signal.positive))
        )
        evolutionStore.save(next)
        return next
    }

    fun reset() {
        secureStore.delete(historyKey)
        evolutionStore.reset()
    }

    private fun history(): List<String> =
        secureStore.get(historyKey)?.toString(Charsets.UTF_8)
            ?.lines()?.filter { it.isNotBlank() }?.takeLast(MAX_HISTORY) ?: emptyList()

    private fun normalize(value: String): String =
        Anonymizer.redact(value).trim().lowercase().replace(Regex("\\s+"), " ")

    companion object {
        const val MAX_HISTORY = 100
    }
}
