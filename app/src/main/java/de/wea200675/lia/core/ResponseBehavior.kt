package de.wea200675.lia.core

/** Explicit response rules shared by prompt construction and the offline fallback. */
object ResponseBehavior {
    private val critiquePattern = Regex(
        """\b(das stimmt nicht|das stimmt so nicht|das ist falsch|du liegst falsch|du hast unrecht|du irrst dich|korrektur|kritik|fehler in deiner antwort|deine antwort ist falsch)\b""",
        RegexOption.IGNORE_CASE
    )
    private val certaintyPattern = Regex(
        """\b(bist du sicher|wie sicher|stimmt das wirklich|weißt du das sicher|weisst du das sicher|beleg(e|en)? das|quelle(n)? dafür|wie zuverlässig)\b""",
        RegexOption.IGNORE_CASE
    )

    fun isCritiqueOrCorrection(text: String): Boolean = critiquePattern.containsMatchIn(text)

    fun asksForCertainty(text: String): Boolean = certaintyPattern.containsMatchIn(text)

    /** Returns an honest fallback only when the user is challenging or checking certainty. */
    fun offlineFeedbackReply(userText: String, previousAnswer: String?): String? {
        if (isCritiqueOrCorrection(userText)) {
            return if (previousAnswer.isNullOrBlank()) {
                "Danke für den Hinweis. Ich nehme Kritik ernst, kann sie im lokalen Grundmodus aber nicht verlässlich prüfen. Welchen konkreten Punkt soll ich korrigieren?"
            } else {
                "Danke, dass du meine letzte Antwort hinterfragst. Ich kann sie im lokalen Grundmodus nicht verlässlich überprüfen und möchte nichts als sicher darstellen. Welcher konkrete Punkt ist falsch oder fehlt?"
            }
        }
        if (asksForCertainty(userText)) {
            return "Das kann ich im lokalen Grundmodus nicht sicher belegen. Ich möchte keine Gewissheit vortäuschen. Wenn du magst, nenne ich dir, was ich weiß und was offen bleibt, sobald ein geprüftes Modell oder eine freigegebene Quelle verfügbar ist."
        }
        return null
    }
}
