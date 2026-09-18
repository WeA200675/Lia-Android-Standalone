package de.wea200675.lia.core

/**
 * Fail-closed boundary for automatic Internet queries. A query containing
 * redacted identifiers or first-person context stays on the device.
 */
object OutboundQueryPolicy {
    private val personalLanguage = Regex(
        """\b(ich|mich|mir|mein(?:e|en|em|er|es)?|wir|uns|unser(?:e|en|em|er|es)?|mutter|vater|tochter|sohn|adresse|wohnort|telefon|konto|arzt|medikament)\b""",
        RegexOption.IGNORE_CASE
    )
    private val redactionMarker = Regex("""\[(E-MAIL|KONTO|TELEFON|DATUM|PLZ)]""")

    fun approved(raw: String): String? {
        val redacted = Anonymizer.redact(raw).trim().replace(Regex("""\s+"""), " ")
        if (redacted.length !in 3..300) return null
        if (redactionMarker.containsMatchIn(redacted)) return null
        if (personalLanguage.containsMatchIn(redacted)) return null
        if (redacted.count { it.isLetter() } < 3) return null
        return redacted
    }
}
