package de.wea200675.lia

import de.wea200675.lia.core.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class KnowledgeIntegrityRuntimeFallbackTest {
    @After fun resetAlarm() = ConfirmedKnowledgeIntegrityRuntime.resetAfterDeletion()

    @Test fun tamperedConfirmedStoreIsBlockedWhileWebAndLocalAiContinue() {
        val store = object : SecureStore {
            override fun put(key: String, value: ByteArray) = Unit
            override fun get(key: String): ByteArray? = throw SecurityException("ciphertext modified")
            override fun delete(key: String) = Unit
        }
        var webCalled = false
        val web = object : WebGateway {
            override suspend fun query(anonymizedQuery: String): Result<String> {
                webCalled = true
                return Result.success("Regenbogen entstehen durch Lichtbrechung.")
            }
        }
        val runtime = object : ModelRuntime {
            override suspend fun generate(prompt: String) = Result.success("Sichere neue Antwort")
            override fun isReady() = true
        }

        val answer = runBlocking {
            AnswerOrchestrator(runtime, web, confirmedKnowledge = ConfirmedKnowledgeRepository(store))
                .answer("Warum entsteht ein Regenbogen?", "Prompt")
        }

        assertTrue(webCalled)
        assertEquals(AnswerSource.LOCAL_AI, answer.source)
        assertEquals(KnowledgeIntegrityState.SECURITY_FAILURE, ConfirmedKnowledgeIntegrityRuntime.snapshot().state)
    }

    @Test fun integrityAlarmKeepsFirstDetectionUntilAdminReset() {
        ConfirmedKnowledgeIntegrityRuntime.recordSecurityFailure(100L)
        ConfirmedKnowledgeIntegrityRuntime.recordSecurityFailure(200L)
        assertEquals(100L, ConfirmedKnowledgeIntegrityRuntime.snapshot().detectedAtEpochMs)

        ConfirmedKnowledgeIntegrityRuntime.resetAfterDeletion()

        assertEquals(KnowledgeIntegrityState.HEALTHY, ConfirmedKnowledgeIntegrityRuntime.snapshot().state)
        assertNull(ConfirmedKnowledgeIntegrityRuntime.snapshot().detectedAtEpochMs)
    }
}
