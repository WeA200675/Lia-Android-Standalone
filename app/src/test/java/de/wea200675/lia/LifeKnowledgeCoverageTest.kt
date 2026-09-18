package de.wea200675.lia

import de.wea200675.lia.core.CoverageLevel
import de.wea200675.lia.core.LifeKnowledgeCoverage
import de.wea200675.lia.core.LifeKnowledgeDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeKnowledgeCoverageTest {
    @Test fun broadLifeAreasAreCategorized() {
        assertEquals(LifeKnowledgeDomain.LANGUAGE, LifeKnowledgeCoverage.assess("Was bedeutet dieses Wort?").domain)
        assertEquals(LifeKnowledgeDomain.TRAVEL, LifeKnowledgeCoverage.assess("Reise nach Hamburg").domain)
        assertEquals(LifeKnowledgeDomain.NATURE, LifeKnowledgeCoverage.assess("Welche Tierart ist das?").domain)
        assertEquals(LifeKnowledgeDomain.TECHNOLOGY, LifeKnowledgeCoverage.assess("Wie funktioniert WLAN?").domain)
        assertEquals(LifeKnowledgeDomain.DAILY_LIFE, LifeKnowledgeCoverage.assess("Hilfe beim Kochen im Alltag").domain)
    }

    @Test fun generalHighStakesEducationRemainsPossible() {
        val health = LifeKnowledgeCoverage.assess("Was ist eine Krankheit?")
        assertEquals(LifeKnowledgeDomain.HEALTH, health.domain)
        assertEquals(CoverageLevel.PARTIAL, health.level)
        assertFalse(health.personalizedDecisionRisk)
    }

    @Test fun personalizedHealthLawAndFinanceDecisionsAreGuarded() {
        assertTrue(LifeKnowledgeCoverage.assess("Soll ich meine Dosis ändern?").personalizedDecisionRisk)
        assertTrue(LifeKnowledgeCoverage.assess("Muss ich diesen Vertrag unterschreiben?").personalizedDecisionRisk)
        assertTrue(LifeKnowledgeCoverage.assess("Soll ich 500 Euro investieren?").personalizedDecisionRisk)
    }

    @Test fun emergenciesAreAlwaysRestricted() {
        val result = LifeKnowledgeCoverage.assess("Ich habe Atemnot")
        assertEquals(LifeKnowledgeDomain.EMERGENCY, result.domain)
        assertEquals(CoverageLevel.RESTRICTED, result.level)
        assertTrue(result.personalizedDecisionRisk)
        assertTrue(LifeKnowledgeCoverage.guardedResponse(result.domain).contains("112"))
    }
}
