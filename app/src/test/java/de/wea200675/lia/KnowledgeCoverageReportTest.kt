package de.wea200675.lia

import de.wea200675.lia.core.CoverageLevel
import de.wea200675.lia.core.KnowledgeCoverageReport
import de.wea200675.lia.core.KnowledgeSourceCatalog
import de.wea200675.lia.core.LifeKnowledgeDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeCoverageReportTest {
    @Test fun everyLifeDomainAppearsExactlyOnce() {
        assertEquals(
            LifeKnowledgeDomain.entries.toSet(),
            KnowledgeCoverageReport.domains.map { it.domain }.toSet()
        )
        assertEquals(LifeKnowledgeDomain.entries.size, KnowledgeCoverageReport.domains.size)
    }

    @Test fun highStakesAndCurrentDomainsAreNotMarkedBroad() {
        val levels = KnowledgeCoverageReport.domains.associate { it.domain to it.level }
        assertEquals(CoverageLevel.PARTIAL, levels[LifeKnowledgeDomain.HEALTH])
        assertEquals(CoverageLevel.PARTIAL, levels[LifeKnowledgeDomain.LAW_ADMINISTRATION])
        assertEquals(CoverageLevel.PARTIAL, levels[LifeKnowledgeDomain.FINANCE_CONSUMER])
        assertEquals(CoverageLevel.PARTIAL, levels[LifeKnowledgeDomain.CURRENT_EVENTS])
        assertEquals(CoverageLevel.RESTRICTED, levels[LifeKnowledgeDomain.EMERGENCY])
    }

    @Test fun adminReportListsAllSourcesAndSafetyBoundary() {
        val report = KnowledgeCoverageReport.adminText()
        KnowledgeSourceCatalog.sources.forEach { assertTrue(report.contains(it.label)) }
        assertTrue(report.contains("Aktive, fest erlaubte Quellen: 17"))
        assertTrue(report.contains("nicht automatisch getroffen"))
    }
}
