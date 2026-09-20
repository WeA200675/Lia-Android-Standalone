package de.wea200675.lia

import de.wea200675.lia.core.ReleaseEvidence
import de.wea200675.lia.core.ReleaseEvidenceLedger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseEvidenceLedgerTest {
    private fun complete() = ReleaseEvidence(
        ciRunId = 123L,
        apkSha256 = "a".repeat(64),
        backupRestoreVerified = true,
        failureInjectionVerified = true,
        samsungDeviceVerified = true,
        recordedAtUtc = "2026-09-20T05:00:00Z"
    )

    @Test fun completeEvidenceIsReleasable() {
        val decision = ReleaseEvidenceLedger.decide(complete())
        assertTrue(decision.releasable)
        assertTrue(decision.missing.isEmpty())
    }

    @Test fun missingDigestBlocksRelease() {
        val decision = ReleaseEvidenceLedger.decide(complete().copy(apkSha256 = "unknown"))
        assertFalse(decision.releasable)
        assertTrue("APK-SHA-256" in decision.missing)
    }

    @Test fun missingHardwareEvidenceBlocksRelease() {
        val decision = ReleaseEvidenceLedger.decide(complete().copy(samsungDeviceVerified = false))
        assertFalse(decision.releasable)
        assertTrue("Samsung-Gerät" in decision.missing)
    }
}
