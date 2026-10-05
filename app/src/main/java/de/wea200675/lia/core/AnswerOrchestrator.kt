package de.wea200675.lia.core

import kotlinx.coroutines.CancellationException

enum class AnswerSource { SAFETY, LOCAL_AI, CONFIRMED_KNOWLEDGE, OFFLINE_FALLBACK }

data class OrchestratedAnswer(
    val text: String,
    val source: AnswerSource,
    val webContextUsed: Boolean = false,
    val knowledgeProvenance: KnowledgeProvenance? = null
)

/**
 * Central answer pipeline. Safety is evaluated first. Web knowledge is optional,
 * anonymized before hand-off, and treated only as untrusted reference data.
 */
class AnswerOrchestrator(
    private val runtime: ModelRuntime,
    private val webGateway: WebGateway? = null,
    private val router: ConversationRouter = ConversationRouter(),
    private val confirmedKnowledge: ConfirmedKnowledgeRepository? = null
) {
    suspend fun answer(
        userText: String,
        prompt: String,
        previousAssistantText: String? = null
    ): OrchestratedAnswer {
        val boundedText = userText.trim().take(2000)
        if (SafetyPolicy.requiresHumanHelp(boundedText)) {
            return OrchestratedAnswer(SafetyPolicy.responseForRisk(), AnswerSource.SAFETY)
        }
        val coverage = LifeKnowledgeCoverage.assess(boundedText)
        if (coverage.personalizedDecisionRisk) {
            return OrchestratedAnswer(
                LifeKnowledgeCoverage.guardedResponse(coverage.domain),
                AnswerSource.SAFETY
            )
        }

        val style = router.classify(boundedText)
        val redacted = Anonymizer.redact(boundedText)
        val retainedResult = if (style == ConversationStyle.KNOWLEDGE) {
            try {
                confirmedKnowledge?.let { repository ->
                    ConfirmedKnowledgeRepository.fingerprintCandidates(redacted)
                        .asSequence()
                        .mapNotNull(repository::find)
                        .firstOrNull()
                        ?.let { retained ->
                            retained.summary to KnowledgeProvenance(
                                sourceLabels = retained.sourceLabels,
                                origin = KnowledgeOrigin.CONFIRMED_STORE,
                                retrievedAtEpochMs = retained.lastUsedAtEpochMs
                            )
                        }
                }
            } catch (_: SecurityException) {
                ConfirmedKnowledgeIntegrityRuntime.recordSecurityFailure()
                null
            }
        } else null
        val webResult = retainedResult ?: if (style == ConversationStyle.KNOWLEDGE && webGateway != null) {
            if (webGateway is ProvenanceWebGateway) {
                recoverableCall { webGateway.queryWithProvenance(redacted) }.getOrNull()?.let { bundle ->
                    UntrustedKnowledgeBoundary.sanitize(bundle.text)
                        ?.takeIf { KnowledgeRelevance.accepts(boundedText, it) }
                        ?.let {
                        it to bundle.provenance
                    }
                }
            } else {
                recoverableCall { webGateway.query(redacted) }.getOrNull()
                    ?.let(UntrustedKnowledgeBoundary::sanitize)
                    ?.let { it to null }
            }
        } else null
        val webContext = webResult?.first

        val enrichedPrompt = if (webContext == null) {
            prompt
        } else {
            "$prompt\n\n${UntrustedKnowledgeBoundary.asReferenceBlock(webContext)}"
        }

        val localAnswer = recoverableCall { runtime.generate(enrichedPrompt) }
            .getOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (localAnswer != null && localAnswer != SafeOfflineRuntime.RESPONSE) {
            return OrchestratedAnswer(
                text = localAnswer,
                source = AnswerSource.LOCAL_AI,
                webContextUsed = webContext != null,
                knowledgeProvenance = webResult?.second
            )
        }

        if (webContext != null) {
            val citedFeedbackText = when {
                ResponseBehavior.isCritiqueOrCorrection(boundedText) ->
                    "Danke, ich prüfe den Hinweis anhand der verfügbaren Quelle. $webContext"
                ResponseBehavior.asksForCertainty(boundedText) ->
                    "Eine verfügbare Quelle dazu nennt: $webContext"
                else -> webContext
            }
            return OrchestratedAnswer(
                text = citedFeedbackText,
                source = if (retainedResult != null) AnswerSource.CONFIRMED_KNOWLEDGE else AnswerSource.OFFLINE_FALLBACK,
                webContextUsed = true,
                knowledgeProvenance = webResult?.second
            )
        }

        if (retainedResult != null) {
            return OrchestratedAnswer(
                text = retainedResult.first,
                source = AnswerSource.CONFIRMED_KNOWLEDGE,
                webContextUsed = true,
                knowledgeProvenance = retainedResult.second
            )
        }

        val feedbackFallback = ResponseBehavior.offlineFeedbackReply(boundedText, previousAssistantText)
        if (feedbackFallback != null) {
            return OrchestratedAnswer(feedbackFallback, AnswerSource.OFFLINE_FALLBACK)
        }

        return OrchestratedAnswer(
            text = router.offlineReply(style),
            source = AnswerSource.OFFLINE_FALLBACK,
            webContextUsed = webContext != null
        )
    }

    /**
     * Normalize recoverable adapter failures without turning cancellation into
     * fallback work. Errors remain visible to the runtime instead of being hidden.
     */
    private suspend fun <T> recoverableCall(block: suspend () -> Result<T>): Result<T> {
        val result = try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Result.failure(failure)
        }
        val failure = result.exceptionOrNull()
        if (failure is CancellationException) throw failure
        return result
    }

}
