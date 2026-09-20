package de.wea200675.lia

import de.wea200675.lia.core.SamsungReleaseAdmission
import de.wea200675.lia.core.SamsungReleaseEvidence
import org.junit.Assert.*
import org.junit.Test

class SamsungReleaseAdmissionTest {
    @Test
    fun incompleteDeviceChecksBlockRelease() {
        val result = SamsungReleaseAdmission.evaluate(
            SamsungReleaseEvidence(true, true, true, false, true, true, true)
        )
        assertFalse(result.deviceReady)
        assertFalse(result.ready)
        assertTrue(result.reason.contains("BLOCKED"))
    }

    @Test
    fun allChecksAreRequired() {
        val result = SamsungReleaseAdmission.evaluate(
            SamsungReleaseEvidence(true, true, true, true, true, true, true)
        )
        assertTrue(result.deviceReady)
        assertTrue(result.releaseReady)
        assertTrue(result.ready)
    }

    @Test
    fun missingRecoveryEvidenceBlocksRelease() {
        val result = SamsungReleaseAdmission.evaluate(
            SamsungReleaseEvidence(true, true, true, true, true, false, true)
        )
        assertTrue(result.deviceReady)
        assertFalse(result.releaseReady)
        assertFalse(result.ready)
    }
}
