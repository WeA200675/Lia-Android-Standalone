package de.wea200675.lia

import de.wea200675.lia.core.WebAccessMode
import de.wea200675.lia.core.WebNetworkAdmission
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebNetworkAdmissionTest {
    @Test fun networkIsDeniedByDefaultAndWithoutRecordedConsent() {
        assertFalse(WebNetworkAdmission.allowed(WebAccessMode.OFFLINE, false))
        assertFalse(WebNetworkAdmission.allowed(WebAccessMode.AUTO_ANONYMIZED_GENERIC, false))
    }

    @Test fun onlyExplicitlyConsentedGenericModeIsAdmitted() {
        assertTrue(WebNetworkAdmission.allowed(WebAccessMode.AUTO_ANONYMIZED_GENERIC, true))
        assertFalse(WebNetworkAdmission.allowed(WebAccessMode.OFFLINE, true))
        assertFalse(WebNetworkAdmission.allowed(WebAccessMode.ASK_BEFORE_PERSONAL, true))
    }

    @Test fun revokingConsentImmediatelyBlocksNetwork() {
        assertTrue(WebNetworkAdmission.allowed(WebAccessMode.AUTO_ANONYMIZED_GENERIC, true))
        assertFalse(WebNetworkAdmission.allowed(WebAccessMode.AUTO_ANONYMIZED_GENERIC, false))
    }
}
