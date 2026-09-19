package de.wea200675.lia.core

import java.util.Locale

/** Rejects source snippets that are plainly unrelated to the asked question. */
object KnowledgeRelevance {
    private val stopWords = setOf(
        "aber","auch","dass","eine","einer","eines","entsteht","für","hat","ist",
        "kann","mit","oder","sich","sind","über","und","von","warum","was","wie",
        "wird","wo","wer","wann","wieso","zur","zum","den","dem","der","die"
    )
    private val tokenPattern = Regex("""[\p{L}\p{N}]{4,}""")

    fun accepts(question: String, answer: String): Boolean {
        val questionTerms = terms(question)
        if (questionTerms.isEmpty()) return true
        val answerContent = if (answer.startsWith("[") && answer.contains("] ")) answer.substringAfter("] ") else answer
        val answerTerms = terms(answerContent)
        return questionTerms.any { term ->
            answerTerms.any { candidate ->
                candidate == term || candidate.startsWith(term) || term.startsWith(candidate)
            }
        }
    }

    private fun terms(text: String): Set<String> =
        tokenPattern.findAll(text.lowercase(Locale.ROOT))
            .map { it.value }
            .filterNot { it in stopWords }
            .toSet()
}
