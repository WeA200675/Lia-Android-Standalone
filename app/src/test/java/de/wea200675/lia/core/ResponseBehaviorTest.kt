package de.wea200675.lia.core

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseBehaviorTest {
    @Test fun recognizesCorrectionsWithoutTreatingEveryQuestionAsCriticism() {
        assertTrue(ResponseBehavior.isCritiqueOrCorrection("Das stimmt so nicht; prüfe es bitte."))
        assertTrue(ResponseBehavior.isCritiqueOrCorrection("Du liegst daneben."))
        assertFalse(ResponseBehavior.isCritiqueOrCorrection("Was weißt du über Blumen?"))
    }

    @Test fun recognizesRequestsForConfidenceAndEvidence() {
        assertTrue(ResponseBehavior.asksForCertainty("Bist du dir sicher?"))
        assertTrue(ResponseBehavior.asksForCertainty("Kannst du das belegen?"))
        assertFalse(ResponseBehavior.asksForCertainty("Erzähl mir etwas über Blumen."))
    }

    @Test fun offlineFallbackAcknowledgesCriticismAndStatesItsLimits() {
        val reply = ResponseBehavior.offlineFeedbackReply(
            "Das ist falsch.",
            "Die frühere Antwort."
        )
        assertNotNull(reply)
        assertTrue(reply!!.contains("hinterfragst"))
        assertTrue(reply.contains("nicht verlässlich überprüfen"))
    }

    @Test fun orchestratorUsesCalibratedFallbackWhenThereIsNoLocalModel() = runBlocking {
        val prompt = PromptContext.build(
            userText = "Das stimmt nicht.",
            profile = emptyList(),
            onlineAllowed = false,
            previousAssistantText = "Die frühere Antwort."
        )
        val answer = AnswerOrchestrator(SafeOfflineRuntime()).answer(
            userText = "Das stimmt nicht.",
            prompt = prompt,
            previousAssistantText = "Die frühere Antwort."
        )

        assertEquals(AnswerSource.OFFLINE_FALLBACK, answer.source)
        assertTrue(answer.text.contains("hinterfragst"))
        assertTrue(answer.text.contains("nicht verlässlich überprüfen"))
    }

    @Test fun certaintyQuestionGetsExplicitUncertaintyFallback() {
        val reply = ResponseBehavior.offlineFeedbackReply("Bist du dir sicher?", null)
        assertNotNull(reply)
        assertTrue(reply!!.contains("keine Gewissheit vortäuschen"))
    }
}
