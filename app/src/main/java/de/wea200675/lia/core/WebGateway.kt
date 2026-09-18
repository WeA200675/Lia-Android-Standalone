package de.wea200675.lia.core

/** Redacts common direct identifiers before any approved web request leaves the device. */
object Anonymizer {
    private val email = Regex("[\\\\w.+-]+@[\\\\w.-]+\\\\.[A-Za-z]{2,}")
    private val iban = Regex("\\\\b[A-Z]{2}\\\\d{2}(?:\\\\s?[A-Z0-9]){11,30}\\\\b")
    private val phone = Regex("(?<!\\\\w)(?:\\\\+?\\\\d[\\\\d\\\\s()/.-]{6,}\\\\d)(?!\\\\w)")
    private val date = Regex("\\\\b\\\\d{1,2}[./-]\\\\d{1,2}[./-]\\\\d{2,4}\\\\b")
    private val postalCode = Regex("\\\\b\\\\d{5}\\\\b")

    fun redact(input: String): String = input
        .replace(email, "[E-MAIL]")
        .replace(iban, "[KONTO]")
        .replace(phone, "[TELEFON]")
        .replace(date, "[DATUM]")
        .replace(postalCode, "[PLZ]")
}
