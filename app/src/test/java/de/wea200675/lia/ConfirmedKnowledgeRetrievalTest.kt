package de.wea200675.lia

import de.wea200675.lia.core.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ConfirmedKnowledgeRetrievalTest {
    private class MemoryStore : SecureStore {
        private val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value }
        override fun get(key: String): ByteArray? = values[key]
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun exactAnonymizedQuestionUsesConfirmedKnowledgeWithoutNetwork() {
        val repository = ConfirmedKnowledgeRepository(MemoryStore())
        val question = "Warum ist der Himmel blau?"
        val redacted = Anonymizer.redact(question)
        assertTrue(repository.saveConfirmed(
            "Blaues Licht wird in der Atmosphäre stärker gestreut.",
            listOf("Wikipedia"),
            ConfirmedKnowledgeRepository.fingerprint(redacted)
        ))
        var webCalled = false
        var modelPrompt = ""
        val web = object : WebGateway {
            override suspend fun query(anonymizedQuery: String): Result<String> {
                webCalled = true
                return Result.success("Netzwerktext")
            }
        }
        val runtime = object : ModelRuntime {
            override suspend fun generate(prompt: String): Result<String> {
                modelPrompt = prompt
                return Result.success("Verständliche Antwort")
            }
            override fun isReady() = true
        }

        val answer = runBlocking {
            AnswerOrchestrator(runtime, web, confirmedKnowledge = repository).answer(question, "Grundprompt")
        }

        assertFalse(webCalled)
        assertTrue(modelPrompt.contains("stärker gestreut"))
        assertEquals(KnowledgeOrigin.CONFIRMED_STORE, answer.knowledgeProvenance?.origin)
        assertEquals(listOf("Wikipedia"), answer.knowledgeProvenance?.sourceLabels)
    }

    @Test fun modelFailureReturnsExactConfirmedSummaryWithSources() {
        val repository = ConfirmedKnowledgeRepository(MemoryStore())
        val question = "Warum entsteht ein Regenbogen?"
        val summary = "Ein Regenbogen entsteht durch Brechung und Reflexion des Lichts in Wassertropfen."
        assertTrue(repository.saveConfirmed(
            summary,
            listOf("Wikipedia", "Wikidata"),
            ConfirmedKnowledgeRepository.fingerprint(Anonymizer.redact(question))
        ))
        var webCalled = false
        val web = object : WebGateway {
            override suspend fun query(anonymizedQuery: String): Result<String> {
                webCalled = true
                return Result.success("Netzwerktext")
            }
        }
        val runtime = object : ModelRuntime {
            override suspend fun generate(prompt: String) = Result.failure<String>(IllegalStateException("Modell ausgefallen"))
            override fun isReady() = false
        }

        val answer = runBlocking {
            AnswerOrchestrator(runtime, web, confirmedKnowledge = repository).answer(question, "Grundprompt")
        }

        assertFalse(webCalled)
        assertEquals(AnswerSource.CONFIRMED_KNOWLEDGE, answer.source)
        assertEquals(summary, answer.text)
        assertEquals(KnowledgeOrigin.CONFIRMED_STORE, answer.knowledgeProvenance?.origin)
        assertEquals(listOf("Wikipedia", "Wikidata"), answer.knowledgeProvenance?.sourceLabels)
    }

    @Test fun safetyGateNeverReadsConfirmedKnowledge() {
        val store = object : SecureStore {
            override fun put(key: String, value: ByteArray) = Unit
            override fun get(key: String): ByteArray? = throw AssertionError("store must not be read")
            override fun delete(key: String) = Unit
        }
        val runtime = object : ModelRuntime {
            override suspend fun generate(prompt: String) = throw AssertionError("model must not run")
            override fun isReady() = true
        }

        val answer = runBlocking {
            AnswerOrchestrator(runtime, confirmedKnowledge = ConfirmedKnowledgeRepository(store))
                .answer("Ich habe starke Brustschmerzen", "prompt")
        }

        assertEquals(AnswerSource.SAFETY, answer.source)
    }
}
