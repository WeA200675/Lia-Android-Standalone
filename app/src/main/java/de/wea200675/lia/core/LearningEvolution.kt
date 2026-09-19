package de.wea200675.lia.core

enum class DevelopmentStage { FOUNDATION, EMPATHY, DIALOGUE, AUTONOMY }

enum class LearningSignalKind { DAILY_IMPULSE, FEEDBACK, INTEREST }

data class LearningSignal(
    val kind: LearningSignalKind,
    val text: String,
    val positive: Boolean = true
)

data class LearningEvolutionState(
    val stage: DevelopmentStage,
    val acceptedSignals: Int,
    val rejectedSignals: Int,
    val focusAreas: Set<String>
)

object LearningEvolution {
    const val MAX_SIGNALS_PER_DAY = 25
    const val MAX_SIGNAL_LENGTH = 500

    fun accept(state: LearningEvolutionState, signals: List<LearningSignal>): LearningEvolutionState {
        val bounded = signals.asSequence()
            .filter { it.text.trim().isNotEmpty() && it.text.length <= MAX_SIGNAL_LENGTH }
            .take(MAX_SIGNALS_PER_DAY)
            .toList()
        val accepted = bounded.count { it.positive }
        val rejected = bounded.size - accepted
        val nextCount = state.acceptedSignals + accepted
        val nextStage = when {
            nextCount >= 200 -> DevelopmentStage.AUTONOMY
            nextCount >= 100 -> DevelopmentStage.DIALOGUE
            nextCount >= 40 -> DevelopmentStage.EMPATHY
            else -> DevelopmentStage.FOUNDATION
        }
        val areas = bounded.map { it.kind.name.lowercase() }.toSet()
        return state.copy(
            stage = if (nextStage.ordinal >= state.stage.ordinal) nextStage else state.stage,
            acceptedSignals = nextCount,
            rejectedSignals = state.rejectedSignals + rejected,
            focusAreas = (state.focusAreas + areas).take(8).toSet()
        )
    }
}
