package de.wea200675.lia

import de.wea200675.lia.core.OutboundQueryPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OutboundQueryPolicyTest {
    @Test fun allowsGenericKnowledgeQuestion() {
        assertEquals(
            "Warum entsteht ein Regenbogen?",
            OutboundQueryPolicy.approved("Warum entsteht ein Regenbogen?")
        )
    }

    @Test fun rejectsDirectIdentifiersInsteadOfSendingPlaceholders() {
        assertNull(OutboundQueryPolicy.approved("Wetter für 12345"))
        assertNull(OutboundQueryPolicy.approved("Antwort an anna@example.org"))
        assertNull(OutboundQueryPolicy.approved("Rufe +49 170 1234567 an"))
    }

    @Test fun rejectsPersonalAndMedicalContext() {
        assertNull(OutboundQueryPolicy.approved("Welche Medikamente nehme ich?"))
        assertNull(OutboundQueryPolicy.approved("Wo wohnt meine Mutter?"))
        assertNull(OutboundQueryPolicy.approved("Was weißt du über meinen Arzt?"))
    }

    @Test fun boundsAndNormalizesApprovedQuery() {
        assertEquals(
            "Wie funktioniert Photosynthese?",
            OutboundQueryPolicy.approved("  Wie   funktioniert   Photosynthese?  ")
        )
        assertNull(OutboundQueryPolicy.approved("a".repeat(301)))
        assertNull(OutboundQueryPolicy.approved("??"))
    }
}
