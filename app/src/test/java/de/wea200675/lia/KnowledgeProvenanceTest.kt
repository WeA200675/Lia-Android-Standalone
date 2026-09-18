package de.wea200675.lia

import de.wea200675.lia.core.AnswerOrchestrator
import de.wea200675.lia.core.AnswerSource
import de.wea200675.lia.core.BoundedKnowledgeCache
import de.wea200675.lia.core.KnowledgeBundle
import de.wea200675.lia.core.KnowledgeFreshnessPolicy
import de.wea200675.lia.core.KnowledgeOrigin
import de.wea200675.lia.core.KnowledgeProvenance
import de.wea200675.lia.core.KnowledgeSourceLabels
import de.wea200675.lia.core.ModelRuntime
import de.wea200675.lia.core.ProvenanceWebGateway
import de.wea200675.lia.core.SafeWikipediaGateway
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeProvenanceTest {
    @Test fun sourceLabelsAreRecoveredFromCachedRenderedContext() {
        val labels = KnowledgeSourceLabels.fromRenderedContext(
            "[Wikipedia (Deutsch)] Titel: Text\n\n[Wikidata] Objekt: Wert"
        )
        assertEquals(listOf("Wikipedia (Deutsch)", "Wikidata"), labels)
    }

    @Test fun orchestratorCarriesRichGatewayProvenanceToAnswer() {
        val provenance = KnowledgeProvenance(
            sourceLabels = listOf("Wikipedia (Deutsch)", "Wikidata"),
            origin = KnowledgeOrigin.LIVE,
            retrievedAtEpochMs = 1234L
        )
        val gateway = object : ProvenanceWebGateway {
            override suspend fun queryWithProvenance(anonymizedQuery: String) =
                Result.success(KnowledgeBundle("[Wikipedia (Deutsch)] Himmel: blau", provenance))
        }
        val runtime = object : ModelRuntime {
            override suspend fun generate(prompt: String) = Result.success("Eine Erklärung")
            override fun isReady() = true
        }

        val answer = runSuspend {
            AnswerOrchestrator(runtime, gateway).answer("Warum ist der Himmel blau?", "Grundprompt")
        }

        assertEquals(AnswerSource.LOCAL_AI, answer.source)
        assertTrue(answer.webContextUsed)
        assertEquals(provenance, answer.knowledgeProvenance)
    }

    @Test fun gatewayMarksSessionCacheWithoutNetworkAccess() {
        val cache = BoundedKnowledgeCache()
        val query = "Warum ist der Himmel blau?"
        cache.put(
            query,
            "[Wikipedia (Deutsch)] Himmel: Streuung des Lichts",
            KnowledgeFreshnessPolicy.HOUR_MS
        )
        val gateway = SafeWikipediaGateway(
            enabled = { true },
            cache = cache,
            clock = { 5678L }
        )

        val bundle = runSuspend { gateway.queryWithProvenance(query).getOrThrow() }

        assertEquals(KnowledgeOrigin.SESSION_CACHE, bundle.provenance.origin)
        assertEquals(5678L, bundle.provenance.retrievedAtEpochMs)
        assertEquals(listOf("Wikipedia (Deutsch)"), bundle.provenance.sourceLabels)
    }

    @Test fun legacyGatewayAnswersRemainCompatibleWithoutInventedProvenance() {
        val gateway = object : de.wea200675.lia.core.WebGateway {
            override suspend fun query(anonymizedQuery: String) =
                Result.success("[Quelle] Fakt")
        }
        val runtime = object : ModelRuntime {
            override suspend fun generate(prompt: String) = Result.success("Antwort")
            override fun isReady() = true
        }

        val answer = runSuspend {
            AnswerOrchestrator(runtime, gateway).answer("Warum regnet es?", "Prompt")
        }

        assertTrue(answer.webContextUsed)
        assertEquals(null, answer.knowledgeProvenance)
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var outcome: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) { outcome = result }
        })
        return checkNotNull(outcome).getOrThrow()
    }
}
