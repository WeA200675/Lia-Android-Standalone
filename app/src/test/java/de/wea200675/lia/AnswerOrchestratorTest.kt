package de.wea200675.lia

import de.wea200675.lia.core.AnswerOrchestrator
import de.wea200675.lia.core.AnswerSource
import de.wea200675.lia.core.ModelRuntime
import de.wea200675.lia.core.WebGateway
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerOrchestratorTest {
    @Test fun safetyGateRunsBeforeModelAndWeb() {
        var modelCalled = false
        var webCalled = false
        val runtime = runtime {
            modelCalled = true
            Result.success("nicht verwenden")
        }
        val web = gateway {
            webCalled = true
            Result.success("nicht verwenden")
        }

        val answer = runSuspend {
            AnswerOrchestrator(runtime, web).answer("Ich habe starke Brustschmerzen", "prompt")
        }

        assertEquals(AnswerSource.SAFETY, answer.source)
        assertFalse(modelCalled)
        assertFalse(webCalled)
    }

    @Test fun knowledgeQuestionUsesRedactedContextWithLocalModel() {
        var sentToWeb = ""
        var promptToModel = ""
        val web = gateway {
            sentToWeb = it
            Result.success("Ein Regenbogen entsteht durch Lichtbrechung.")
        }
        val runtime = runtime {
            promptToModel = it
            Result.success("Hier ist die verständliche Erklärung.")
        }

        val answer = runSuspend {
            AnswerOrchestrator(runtime, web).answer(
                "Warum entsteht ein Regenbogen? Kontakt anna@example.org",
                "Grundprompt"
            )
        }

        assertEquals(AnswerSource.LOCAL_AI, answer.source)
        assertTrue(answer.webContextUsed)
        assertFalse(sentToWeb.contains("anna@example.org"))
        assertTrue(sentToWeb.contains("[E-MAIL]"))
        assertTrue(promptToModel.contains("Lichtbrechung"))
    }

    @Test fun localFailureFallsBackWithoutWebForPracticalHelp() {
        var webCalled = false
        val runtime = runtime { Result.failure(IllegalStateException("Modell aus")) }
        val web = gateway {
            webCalled = true
            Result.success("nicht verwenden")
        }

        val answer = runSuspend {
            AnswerOrchestrator(runtime, web).answer("Hilf mir beim Aufräumen", "prompt")
        }

        assertEquals(AnswerSource.OFFLINE_FALLBACK, answer.source)
        assertFalse(webCalled)
        assertTrue(answer.text.isNotBlank())
    }

    private fun runtime(block: suspend (String) -> Result<String>) = object : ModelRuntime {
        override suspend fun generate(prompt: String) = block(prompt)
        override fun isReady() = true
    }

    private fun gateway(block: suspend (String) -> Result<String>) = object : WebGateway {
        override suspend fun query(anonymizedQuery: String) = block(anonymizedQuery)
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var outcome: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                outcome = result
            }
        })
        return checkNotNull(outcome).getOrThrow()
    }
}
