package de.wea200675.lia.core

enum class LifeKnowledgeDomain {
    GENERAL, DAILY_LIFE, LANGUAGE, EDUCATION, HISTORY_CULTURE, NATURE,
    TRAVEL, TECHNOLOGY, CURRENT_EVENTS, HEALTH, LAW_ADMINISTRATION,
    FINANCE_CONSUMER, EMERGENCY
}

enum class CoverageLevel { BROAD, PARTIAL, RESTRICTED }

data class CoverageAssessment(
    val domain: LifeKnowledgeDomain,
    val level: CoverageLevel,
    val personalizedDecisionRisk: Boolean
)

object LifeKnowledgeCoverage {
    private val emergency = Regex("""\b(brustschmerz|atemnot|bewusstlos|schlaganfall|suizid|notfall|112)\b""", RegexOption.IGNORE_CASE)
    private val health = Regex("""\b(gesundheit|krankheit|symptom|diagnose|therapie|medikament|dosis|arzt|blutdruck|schmerz)\b""", RegexOption.IGNORE_CASE)
    private val law = Regex("""\b(recht|gesetz|vertrag|kündigung|frist|klage|anwalt|behörde|rente|pflegegrad)\b""", RegexOption.IGNORE_CASE)
    private val finance = Regex("""\b(geld|konto|überweisung|kredit|anlage|invest|steuer|versicherung|kaufen|preis)\b""", RegexOption.IGNORE_CASE)
    private val travel = Regex("""\b(reise|urlaub|hotel|sehenswürdigkeit|wandern)\b""", RegexOption.IGNORE_CASE)
    private val technology = Regex("""\b(computer|tablet|android|wlan|software|programmierung|internet)\b""", RegexOption.IGNORE_CASE)
    private val language = Regex("""\b(bedeutung|definition|wort|sprache|übersetze)\b""", RegexOption.IGNORE_CASE)
    private val education = Regex("""\b(lernen|lektion|kurs|übung|erkläre)\b""", RegexOption.IGNORE_CASE)
    private val historyCulture = Regex("""\b(geschichte|historisch|kultur|kunst|literatur|gedicht|zitat)\b""", RegexOption.IGNORE_CASE)
    private val nature = Regex("""\b(tier|pflanze|natur|spezies|gattung|wetter)\b""", RegexOption.IGNORE_CASE)
    private val current = Regex("""\b(aktuell|heute|nachricht|neuigkeit)\b""", RegexOption.IGNORE_CASE)
    private val daily = Regex("""\b(haushalt|kochen|rezept|garten|alltag|aufräumen|einkauf)\b""", RegexOption.IGNORE_CASE)
    private val personalDecision = Regex(
        """\b(soll ich|darf ich|muss ich|für mich|meine dosis|mein symptom|unterschreiben|überweisen|investieren|frist verpassen)\b""",
        RegexOption.IGNORE_CASE
    )

    fun assess(query: String): CoverageAssessment {
        val domain = when {
            emergency.containsMatchIn(query) -> LifeKnowledgeDomain.EMERGENCY
            health.containsMatchIn(query) -> LifeKnowledgeDomain.HEALTH
            law.containsMatchIn(query) -> LifeKnowledgeDomain.LAW_ADMINISTRATION
            finance.containsMatchIn(query) -> LifeKnowledgeDomain.FINANCE_CONSUMER
            current.containsMatchIn(query) -> LifeKnowledgeDomain.CURRENT_EVENTS
            travel.containsMatchIn(query) -> LifeKnowledgeDomain.TRAVEL
            technology.containsMatchIn(query) -> LifeKnowledgeDomain.TECHNOLOGY
            language.containsMatchIn(query) -> LifeKnowledgeDomain.LANGUAGE
            education.containsMatchIn(query) -> LifeKnowledgeDomain.EDUCATION
            historyCulture.containsMatchIn(query) -> LifeKnowledgeDomain.HISTORY_CULTURE
            nature.containsMatchIn(query) -> LifeKnowledgeDomain.NATURE
            daily.containsMatchIn(query) -> LifeKnowledgeDomain.DAILY_LIFE
            else -> LifeKnowledgeDomain.GENERAL
        }
        val level = when (domain) {
            LifeKnowledgeDomain.EMERGENCY -> CoverageLevel.RESTRICTED
            LifeKnowledgeDomain.HEALTH,
            LifeKnowledgeDomain.LAW_ADMINISTRATION,
            LifeKnowledgeDomain.FINANCE_CONSUMER,
            LifeKnowledgeDomain.CURRENT_EVENTS -> CoverageLevel.PARTIAL
            else -> CoverageLevel.BROAD
        }
        val risk = domain == LifeKnowledgeDomain.EMERGENCY ||
            (domain in setOf(
                LifeKnowledgeDomain.HEALTH,
                LifeKnowledgeDomain.LAW_ADMINISTRATION,
                LifeKnowledgeDomain.FINANCE_CONSUMER
            ) && personalDecision.containsMatchIn(query))
        return CoverageAssessment(domain, level, risk)
    }

    fun guardedResponse(domain: LifeKnowledgeDomain): String = when (domain) {
        LifeKnowledgeDomain.EMERGENCY ->
            "Das könnte dringend sein. Bitte rufe 112 oder bitte sofort eine anwesende Person um Hilfe."
        LifeKnowledgeDomain.HEALTH ->
            "Ich kann allgemeine Gesundheitsinformationen erklären, aber keine persönliche Diagnose oder Dosierung sicher festlegen. Bitte kläre diese Entscheidung mit einer Ärztin, einem Arzt oder einer Apotheke."
        LifeKnowledgeDomain.LAW_ADMINISTRATION ->
            "Ich kann allgemeine Begriffe erklären, aber keine persönliche Rechts- oder Fristentscheidung zuverlässig treffen. Bitte prüfe das Schreiben mit der zuständigen Stelle oder einer Rechtsberatung."
        LifeKnowledgeDomain.FINANCE_CONSUMER ->
            "Ich kann allgemeine Finanzbegriffe erklären, aber keine persönliche Überweisung, Anlage oder Vertragsentscheidung freigeben. Bitte prüfe Betrag, Empfänger und Bedingungen mit einer vertrauten Person oder Fachstelle."
        else -> "Dazu kann ich allgemeine Informationen geben, aber keine persönliche Entscheidung sicher übernehmen."
    }
}
