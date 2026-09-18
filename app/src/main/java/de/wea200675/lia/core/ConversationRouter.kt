package de.wea200675.lia.core

class ConversationRouter {
    fun classify(text: String): ConversationStyle {
        val normalized = text.lowercase().replace(Regex("""\s+"""), " ").trim()
        return when {
            SMALLTALK.containsMatchIn(normalized) -> ConversationStyle.SMALLTALK
            INTEREST.containsMatchIn(normalized) -> ConversationStyle.INTEREST_DISCOVERY
            KNOWLEDGE_QUESTION.containsMatchIn(normalized) ||
                KNOWLEDGE_TERMS.containsMatchIn(normalized) -> ConversationStyle.KNOWLEDGE
            else -> ConversationStyle.PRACTICAL_HELP
        }
    }

    fun offlineReply(style: ConversationStyle) = when (style) {
        ConversationStyle.SMALLTALK -> "Ich höre dir gern zu. Erzähl mir mehr, wenn du möchtest."
        ConversationStyle.KNOWLEDGE -> KnowledgeFacts.random()
        ConversationStyle.INTEREST_DISCOVERY -> "Das klingt interessant. Möchtest du mir davon erzählen?"
        ConversationStyle.PRACTICAL_HELP -> "Gern. Wir können das gemeinsam Schritt für Schritt angehen."
    }

    fun applySensitivity(reply: String, profile: SensitivityProfile): String {
        var out = reply
        if (profile.gentler) out = "Ganz in Ruhe: $out"
        if (profile.slower) out = out.replace(". ", ".\n")
        return out
    }

    private companion object {
        val SMALLTALK = Regex("""\b(wie geht(?: es|s)?(?: dir| ihnen)?|erzähl(?:e|en)?|mein tag|dein tag|guten morgen|guten abend)\b""")
        val INTEREST = Regex("""\b(interess(?:e|iert|ant)|gern|liebe|mag|hobby|hobbys|lieblings)\b""")
        val KNOWLEDGE_QUESTION = Regex(
            """^(?:bitte\s+)?(?:was (?:ist|sind|bedeutet|passiert|verursacht)|wer (?:ist|war|hat)|wann (?:ist|war|wurde)|wo (?:ist|liegt|findet)|warum|wieso|weshalb|wie (?:entsteht|funktioniert|wirkt|lange|hoch|alt|groß|weit|viel|viele|nennt|heißt|wird)|erklär(?:e)?(?: mir)?|weißt du)\b"""
        )
        val KNOWLEDGE_TERMS = Regex("""\b(wissenswert|fakt(?:en)?|definition|bedeutung|ursache|geschichte von)\b""")
    }
}
