package de.wea200675.lia.core

data class SamsungReleaseEvidence(
    val android16Verified: Boolean,
    val kioskAndWifiVerified: Boolean,
    val audioAndRotationVerified: Boolean,
    val restartVerified: Boolean,
    val apkDigestVerified: Boolean,
    val backupRestoreVerified: Boolean,
    val failureInjectionVerified: Boolean
)

data class SamsungReleaseAdmissionResult(
    val deviceReady: Boolean,
    val releaseReady: Boolean,
    val ready: Boolean,
    val reason: String
)

object SamsungReleaseAdmission {
    fun evaluate(evidence: SamsungReleaseEvidence): SamsungReleaseAdmissionResult {
        val deviceReady = evidence.android16Verified &&
            evidence.kioskAndWifiVerified &&
            evidence.audioAndRotationVerified &&
            evidence.restartVerified
        val releaseReady = evidence.apkDigestVerified &&
            evidence.backupRestoreVerified &&
            evidence.failureInjectionVerified
        val reason = when {
            !deviceReady -> "Samsung-Geräteabnahme unvollständig; Release BLOCKED."
            !releaseReady -> "Release-/Recovery-Nachweise unvollständig; Veröffentlichung BLOCKED."
            else -> "Samsung-Abnahme und Release-Härtung sind verifiziert."
        }
        return SamsungReleaseAdmissionResult(deviceReady, releaseReady, deviceReady && releaseReady, reason)
    }
}
