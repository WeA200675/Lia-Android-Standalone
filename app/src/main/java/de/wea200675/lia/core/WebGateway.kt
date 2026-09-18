package de.wea200675.lia.core

/** Gateway contract: only explicitly approved, redacted queries may leave the device. */
interface WebGateway { suspend fun query(anonymizedQuery: String): Result<String> }

/** Redacts common direct identifiers before any approved web request leaves the device. */
object Anonymizer {
    private val email = Regex("""[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}""")
    private val iban = Regex("""\b[A-Z]{2}\d{2}(?:\s?[A-Z0-9]){11,30}\b""")
    private val phone = Regex("""(?<![A-Za-z])\+?[0-9][0-9 ()/.-]{6,}[0-9](?![A-Za-z])""")
    private val date = Regex("""\b\d{1,2}[./-]\d{1,2}[./-]\d{2,4}\b""")
    private val postalCode = Regex("""\b\d{5}\b""")

    fun redact(input: String): String = input
        .replace(email, "[E-MAIL]")
        .replace(iban, "[KONTO]")
        .replace(phone, "[TELEFON]")
        .replace(date, "[DATUM]")
        .replace(postalCode, "[PLZ]")
}
