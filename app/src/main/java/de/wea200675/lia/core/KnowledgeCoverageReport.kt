package de.wea200675.lia.core

data class DomainCoverageSummary(
    val domain: LifeKnowledgeDomain,
    val label: String,
    val level: CoverageLevel
)

object KnowledgeCoverageReport {
    val domains = listOf(
        DomainCoverageSummary(LifeKnowledgeDomain.GENERAL, "Allgemeinwissen", CoverageLevel.BROAD),
        DomainCoverageSummary(LifeKnowledgeDomain.DAILY_LIFE, "Alltag", CoverageLevel.BROAD),
        DomainCoverageSummary(LifeKnowledgeDomain.LANGUAGE, "Sprache", CoverageLevel.BROAD),
        DomainCoverageSummary(LifeKnowledgeDomain.EDUCATION, "Bildung", CoverageLevel.BROAD),
        DomainCoverageSummary(LifeKnowledgeDomain.HISTORY_CULTURE, "Geschichte und Kultur", CoverageLevel.BROAD),
        DomainCoverageSummary(LifeKnowledgeDomain.NATURE, "Natur", CoverageLevel.BROAD),
        DomainCoverageSummary(LifeKnowledgeDomain.TRAVEL, "Reisen", CoverageLevel.BROAD),
        DomainCoverageSummary(LifeKnowledgeDomain.TECHNOLOGY, "Technik", CoverageLevel.BROAD),
        DomainCoverageSummary(LifeKnowledgeDomain.CURRENT_EVENTS, "Aktuelle Informationen", CoverageLevel.PARTIAL),
        DomainCoverageSummary(LifeKnowledgeDomain.HEALTH, "Gesundheit", CoverageLevel.PARTIAL),
        DomainCoverageSummary(LifeKnowledgeDomain.LAW_ADMINISTRATION, "Recht und Behörden", CoverageLevel.PARTIAL),
        DomainCoverageSummary(LifeKnowledgeDomain.FINANCE_CONSUMER, "Finanzen und Verbraucher", CoverageLevel.PARTIAL),
        DomainCoverageSummary(LifeKnowledgeDomain.EMERGENCY, "Notfälle", CoverageLevel.RESTRICTED)
    )

    fun adminText(): String {
        val coverage = domains.joinToString("\n") {
            val marker = when (it.level) {
                CoverageLevel.BROAD -> "● breit"
                CoverageLevel.PARTIAL -> "◐ eingeschränkt"
                CoverageLevel.RESTRICTED -> "◆ Sicherheitsmodus"
            }
            "$marker · ${it.label}"
        }
        val sources = KnowledgeSourceCatalog.sources.joinToString("\n") {
            "• ${it.label} (${it.language})"
        }
        return """
            Wissensabdeckung
            $coverage

            Aktive, fest erlaubte Quellen: ${KnowledgeSourceCatalog.sources.size}
            $sources

            Persönliche Medizin-, Rechts- und Finanzentscheidungen werden nicht automatisch getroffen.
        """.trimIndent()
    }
}
