package de.wea200675.lia

import de.wea200675.lia.core.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class ConfirmedQuestionFingerprintTest {
    private class MemoryStore : SecureStore {
        private val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value }
        override fun get(key: String): ByteArray? = values[key]
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun harmlessFormattingVariantsShareCanonicalFingerprint() {
        val first = ConfirmedKnowledgeRepository.fingerprint("Warum entsteht ein Regenbogen?")
        val second = ConfirmedKnowledgeRepository.fingerprint("  WARUM   entsteht ein Regenbogen!!! ")
        assertEquals(first, second)
    }

    @Test fun differentQuestionsRemainDifferent() {
        assertNotEquals(
            ConfirmedKnowledgeRepository.fingerprint("Warum entsteht ein Regenbogen?"),
            ConfirmedKnowledgeRepository.fingerprint("Warum entsteht ein Gewitter?")
        )
    }

    @Test fun canonicalVariantRetrievesSameConfirmedEntryWithoutNetwork() {
        val repository = ConfirmedKnowledgeRepository(MemoryStore())
        assertTrue(repository.saveConfirmed(
            "Licht wird in Wassertropfen gebrochen und reflektiert.",
            listOf("Wikipedia"),
            ConfirmedKnowledgeRepository.fingerprint("Warum entsteht ein Regenbogen?")
        ))
        var webCalled = false
        val web = object : WebGateway {
            override suspend fun query(anonymizedQuery: String): Result<String> {
                webCalled = true
                return Result.success("Netzwerk")
            }
        }
        val runtime = object : ModelRuntime {
            override suspend fun generate(prompt: String) = Result.failure<String>(IllegalStateException("aus"))
            override fun isReady() = false
        }

        val answer = runBlocking {
            AnswerOrchestrator(runtime, web, confirmedKnowledge = repository)
                .answer("WARUM entsteht ein Regenbogen", "Prompt")
        }

        assertFalse(webCalled)
        assertEquals(AnswerSource.CONFIRMED_KNOWLEDGE, answer.source)
    }

    @Test fun legacyExactFingerprintRemainsReadable() {
        val repository = ConfirmedKnowledgeRepository(MemoryStore())
        val question = "Warum ist Wasser nass?"
        val legacy = MessageDigest.getInstance("SHA-256")
            .digest(question.trim().toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        assertTrue(repository.saveConfirmed("Wasser benetzt Oberflächen.", listOf("Quelle"), legacy))

        val candidates = ConfirmedKnowledgeRepository.fingerprintCandidates(question)

        assertTrue(candidates.contains(legacy))
        assertNotNull(candidates.asSequence().mapNotNull(repository::find).firstOrNull())
    }
}
