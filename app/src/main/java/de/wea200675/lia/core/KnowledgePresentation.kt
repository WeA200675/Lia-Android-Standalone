package de.wea200675.lia.core

/** User-facing provenance deliberately avoids internal gateway labels and timestamps. */
object KnowledgePresentation {
    fun sourceLine(provenance: KnowledgeProvenance?): String = when (provenance?.origin) {
        null -> ""
        KnowledgeOrigin.LIVE -> "\n\nℹ Wissen aus dem Internet (anonymisierte Anfrage)"
        KnowledgeOrigin.SESSION_CACHE -> "\n\nℹ Wissen aus dem Sitzungspuffer"
        KnowledgeOrigin.CONFIRMED_STORE -> "\n\nℹ Wissen aus bestätigtem lokalem Wissen"
    }
}
