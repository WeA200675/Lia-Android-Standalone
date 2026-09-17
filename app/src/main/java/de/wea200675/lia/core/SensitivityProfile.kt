package de.wea200675.lia.core

/** Safe, local conversation preferences learned from explicit user feedback. */
data class SensitivityProfile(
    val slower: Boolean = false,
    val gentler: Boolean = false,
    val avoidTopics: Set<String> = emptySet(),
    val preferredStyle: String = "warm"
) {
    fun apply(feedback: FeedbackSignal): SensitivityProfile = when (feedback) {
        FeedbackSignal.SLOWER -> copy(slower = true)
        FeedbackSignal.GENTLER -> copy(gentler = true)
        FeedbackSignal.CHANGE_TOPIC -> copy(preferredStyle = "respect-boundaries")
        is FeedbackSignal.AVOID_TOPIC -> copy(avoidTopics = avoidTopics + feedback.topic)
        FeedbackSignal.RESET -> SensitivityProfile()
    }
}

sealed class FeedbackSignal {
    data object SLOWER : FeedbackSignal()
    data object GENTLER : FeedbackSignal()
    data object CHANGE_TOPIC : FeedbackSignal()
    data class AVOID_TOPIC(val topic: String) : FeedbackSignal()
    data object RESET : FeedbackSignal()
}

/** Explicit feedback is required before a preference changes. */
object FeedbackInterpreter {
    fun parse(text: String): FeedbackSignal? {
        val value = text.trim().lowercase()
        return when {
            value.contains("langsamer") || value.contains("pause") -> FeedbackSignal.SLOWER
            value.contains("zu direkt") || value.contains("einfühlsam") || value.contains("feinfühlig") -> FeedbackSignal.GENTLER
            value.contains("anderes thema") || value.contains("wechseln") -> FeedbackSignal.CHANGE_TOPIC
            value == "zurücksetzen" || value == "reset" -> FeedbackSignal.RESET
            else -> null
        }
    }
}
