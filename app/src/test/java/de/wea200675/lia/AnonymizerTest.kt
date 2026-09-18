package de.wea200675.lia

import de.wea200675.lia.core.Anonymizer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnonymizerTest {
    @Test fun redactsDirectIdentifiersBeforeWebUse() {
        val input = "Kontakt anna@example.org, Telefon +49 170 1234567, IBAN DE89 3704 0044 0532 0130 00, am 18.09.2026 in 12345."
        val redacted = Anonymizer.redact(input)
        assertTrue(redacted.contains("[E-MAIL]"))
        assertTrue(redacted.contains("[TELEFON]"))
        assertTrue(redacted.contains("[KONTO]"))
        assertTrue(redacted.contains("[DATUM]"))
        assertTrue(redacted.contains("[PLZ]"))
        assertFalse(redacted.contains("anna@example.org"))
        assertFalse(redacted.contains("DE89"))
        assertFalse(redacted.contains("12345"))
    }

    @Test fun leavesGenericKnowledgeQuestionReadable() {
        val input = "Wie entsteht ein Regenbogen?"
        assertTrue(Anonymizer.redact(input).contains(input))
    }
}
