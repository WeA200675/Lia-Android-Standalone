package de.wea200675.lia.core

enum class AnswerSource { SAFETY, LOCAL_AI, OFFLINE_FALLBACK }

data class OrchestratedAnswer(
    val text: String,
    val source: AnswerSource,
    val webContextUsed: Boolean = false
)

/**
 * Central answer pipeline. Safety is evaluated first. Web knowledge is optional,
 * anonymized before hand-off, and can only enrich the local model prompt.
 */
class AnswerOrchestrator(
    private val runtime: ModelRuntime,
    private val webGateway: WebGateway? = null,
    private val router: ConversationRouter = ConversationRouter()
) {
    suspend fun answer(userText: String, prompt: String): OrchestratedAnswer {
        val boundedText = userText.trim().take(2000)
        if (SafetyPolicy.requiresHumanHelp(boundedText)) {
            return OrchestratedAnswer(SafetyPolicy.responseForRisk(), AnswerSource.SAFETY)
        }

        val style = router.classify(boundedText)
        val webContext = if (style == ConversationStyle.KNOWLEDGE && webGateway != null) {
            webGateway.query(Anonymizer.redact(boundedText))
                .getOrNull()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.take(4000)
        } else {
            null
        }

        val enrichedPrompt = if (webContext == null) {
            prompt
        } else {
            "$prompt\n\nZusatzwissen aus der freigegebenen Web-/Offline-Kaskade:\n$webContext"
        }

        val localAnswer = runtime.generate(enrichedPrompt)
            .getOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (localAnswer != null) {
            return OrchestratedAnswer(
                text = localAnswer,
                source = AnswerSource.LOCAL_AI,
                webContextUsed = webContext != null
            )
        }

        return OrchestratedAnswer(
            text = router.offlineReply(style),
            source = AnswerSource.OFFLINE_FALLBACK,
            webContextUsed = webContext != null
        )
    }
}
