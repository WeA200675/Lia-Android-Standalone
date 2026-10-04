package de.wea200675.lia

import de.wea200675.lia.core.CompanionResponsePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionResponsePolicyTest {
    @Test fun addsEmpathyForEmotionalContext() {
        assertEquals(
            "Das klingt belastend. Ich bin bei dir.",
            CompanionResponsePolicy.adapt("Ich bin bei dir.", "Ich bin heute sehr traurig.")
        )
    }

    @Test fun doesNotDuplicateEmpathy() {
        assertEquals(
            "Das klingt belastend. Ich bin bei dir.",
            CompanionResponsePolicy.adapt("Das klingt belastend. Ich bin bei dir.", "Ich bin traurig.")
        )
    }

    @Test fun leavesNeutralAnswersUnchanged() {
        assertEquals("Die Antwort.", CompanionResponsePolicy.adapt("Die Antwort.", "Wie funktioniert Regen?"))
    }
}
