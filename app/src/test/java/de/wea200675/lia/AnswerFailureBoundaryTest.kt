package de.wea200675.lia

import de.wea200675.lia.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AnswerFailureBoundaryTest {
    private fun runtime(block: suspend () -> Result<String>) = object : ModelRuntime {
        override suspend fun generate(prompt: String) = block()
        override fun isReady() = true
    }
    private fun web(block: suspend () -> Result<String>) = object : WebGateway {
        override suspend fun query(anonymizedQuery: String) = block()
    }

    @Test fun thrownWebFailureStillAllowsLocalAnswer() = runBlocking {
        val answer = AnswerOrchestrator(
            runtime { Result.success("Lokale Erklärung") },
            web { throw IllegalStateException("network adapter unavailable") }
        ).answer("Warum ist der Himmel blau?", "prompt")
        assertEquals(AnswerSource.LOCAL_AI, answer.source)
        assertEquals("Lokale Erklärung", answer.text)
        assertFalse(answer.webContextUsed)
    }

    @Test fun thrownModelFailureProducesUsableOfflineReply() = runBlocking {
        val answer = AnswerOrchestrator(
            runtime { throw IllegalStateException("native adapter unavailable") }
        ).answer("Hilf mir beim Aufräumen", "prompt")
        assertEquals(AnswerSource.OFFLINE_FALLBACK, answer.source)
        assertTrue(answer.text.isNotBlank())
    }

    @Test fun webCancellationNeverStartsLocalInference() = runBlocking {
        for (returnedFailure in listOf(false, true)) {
            var calls = 0
            val cancelled = CancellationException("cancel")
            val orchestrator = AnswerOrchestrator(
                runtime { calls++; Result.success("unexpected") },
                web { if (returnedFailure) Result.failure(cancelled) else throw cancelled }
            )
            try {
                orchestrator.answer("Warum ist der Himmel blau?", "prompt")
                fail("Cancellation must propagate")
            } catch (actual: CancellationException) {
                assertSame(cancelled, actual)
            }
            assertEquals(0, calls)
        }
    }

    @Test fun modelCancellationNeverProducesFallbackAnswer() = runBlocking {
        for (returnedFailure in listOf(false, true)) {
            val cancelled = CancellationException("cancel")
            try {
                AnswerOrchestrator(runtime {
                    if (returnedFailure) Result.failure(cancelled) else throw cancelled
                }).answer("Hilf mir beim Aufräumen", "prompt")
                fail("Cancellation must propagate")
            } catch (actual: CancellationException) {
                assertSame(cancelled, actual)
            }
        }
    }
}
