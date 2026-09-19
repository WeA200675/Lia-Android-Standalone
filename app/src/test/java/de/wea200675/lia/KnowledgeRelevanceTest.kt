package de.wea200675.lia

import de.wea200675.lia.core.KnowledgeRelevance
import org.junit.Assert.*
import org.junit.Test

class KnowledgeRelevanceTest {
    @Test fun rejectsClearlyUnrelatedSource() {
        assertFalse(KnowledgeRelevance.accepts(
            "Wie entsteht der Regenbogen?",
            "Bienen können Gesichter anhand von Mustern unterscheiden."
        ))
    }

    @Test fun acceptsRelevantGermanSourceWithInflection() {
        assertTrue(KnowledgeRelevance.accepts(
            "Wie entsteht der Regenbogen?",
            "Ein Regenbogen entsteht durch Lichtbrechung in Regentropfen."
        ))
    }

    @Test fun acceptsNamedPersonAnswer() {
        assertTrue(KnowledgeRelevance.accepts(
            "Wer war Goethe?",
            "Johann Wolfgang von Goethe war ein deutscher Dichter."
        ))
    }
    @Test fun sourceLabelAloneDoesNotMakeUnrelatedContentRelevant() {
        assertFalse(KnowledgeRelevance.accepts(
            "Was ist Wikipedia?",
            "[Wikipedia] Bienen können Gesichter anhand von Mustern unterscheiden."
        ))
    }
}
