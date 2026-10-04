package de.wea200675.lia.core

object CompanionResponsePolicy {
    private val emotionalMarkers = listOf("traurig", "einsam", "angst", "sorge", "überfordert", "weine")

    fun adapt(answer: String, userText: String): String {
        val clean = answer.trim()
        if (clean.isEmpty()) return clean
        val normalized = userText.lowercase()
        if (emotionalMarkers.any(normalized::contains) &&
            !clean.startsWith("Das klingt belastend", ignoreCase = true)
        ) {
            return "Das klingt belastend. $clean"
        }
        return clean
    }
}
