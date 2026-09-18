package de.wea200675.lia

import de.wea200675.lia.core.ConversationRouter
import de.wea200675.lia.core.ConversationStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationRouterMatrixTest {
    private val router = ConversationRouter()

    @Test fun commonGermanKnowledgeQuestionsReachKnowledgePath() {
        listOf(
            "Wie entsteht ein Regenbogen?",
            "Wie funktioniert ein Elektromotor?",
            "Was ist Photosynthese?",
            "Wer war Albert Einstein?",
            "Wann wurde Berlin gegründet?",
            "Wo liegt die Nordsee?",
            "Wieso leuchten Sterne?",
            "Erklär mir bitte die Jahreszeiten.",
            "Weißt du etwas über Vulkane?"
        ).forEach { text ->
            assertEquals(text, ConversationStyle.KNOWLEDGE, router.classify(text))
        }
    }

    @Test fun socialAndInterestQuestionsStayOutOfKnowledgePath() {
        mapOf(
            "Wie geht es dir heute?" to ConversationStyle.SMALLTALK,
            "Erzähl mir von deinem Tag." to ConversationStyle.SMALLTALK,
            "Was magst du gern?" to ConversationStyle.INTEREST_DISCOVERY,
            "Mein Hobby ist Gartenarbeit." to ConversationStyle.INTEREST_DISCOVERY,
            "Hilf mir beim Aufräumen." to ConversationStyle.PRACTICAL_HELP
        ).forEach { (text, expected) ->
            assertEquals(text, expected, router.classify(text))
        }
    }

    @Test fun normalizationHandlesCaseAndRepeatedSpaces() {
        assertEquals(
            ConversationStyle.KNOWLEDGE,
            router.classify("  WIE   FUNKTIONIERT ein Radio?  ")
        )
    }
}
