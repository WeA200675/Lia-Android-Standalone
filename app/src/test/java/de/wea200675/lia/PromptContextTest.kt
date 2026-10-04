package de.wea200675.lia

import de.wea200675.lia.core.LearningItem
import de.wea200675.lia.core.PromptContext
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptContextTest {
    @Test fun buildsReadableOfflinePromptWithOnlyConfirmedMemories() {
        val prompt = PromptContext.build(
            userText = "Wie geht es?",
            profile = listOf(
                LearningItem("confirmed", "Mag gern Blumen", confirmed = true),
                LearningItem("unconfirmed", "Private Notiz", confirmed = false)
            ),
            onlineAllowed = false
        )

        assertTrue(prompt.contains("vollständig offline"))
        assertTrue(prompt.contains("\nSicherheitsregeln:"))
        assertTrue(prompt.contains("- Mag gern Blumen"))
        assertFalse(prompt.contains("Private Notiz"))
        assertTrue(prompt.endsWith("Nachricht: Wie geht es?"))
    }
}
