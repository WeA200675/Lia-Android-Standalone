package de.wea200675.lia.core

data class ReleaseEvidence(
    val ciRunId: Long?,
    val apkSha256: String?,
    val backupRestoreVerified: Boolean,
    val failureInjectionVerified: Boolean,
    val samsungDeviceVerified: Boolean,
    val recordedAtUtc: String?
) {
    fun isComplete(): Boolean =
        ciRunId != null && ciRunId > 0 &&
            apkSha256?.matches(Regex("^[0-9a-fA-F]{64}$")) == true &&
            backupRestoreVerified &&
            failureInjectionVerified &&
            samsungDeviceVerified &&
            !recordedAtUtc.isNullOrBlank()
}

data class ReleaseEvidenceDecision(
    val releasable: Boolean,
    val missing: List<String>
)

object ReleaseEvidenceLedger {
    fun decide(evidence: ReleaseEvidence): ReleaseEvidenceDecision {
        val missing = buildList {
            if (evidence.ciRunId == null || evidence.ciRunId <= 0) add("CI-Lauf")
            if (evidence.apkSha256?.matches(Regex("^[0-9a-fA-F]{64}$")) != true) add("APK-SHA-256")
            if (!evidence.backupRestoreVerified) add("Backup/Restore")
            if (!evidence.failureInjectionVerified) add("Fehler-Injection")
            if (!evidence.samsungDeviceVerified) add("Samsung-Gerät")
            if (evidence.recordedAtUtc.isNullOrBlank()) add("Zeitstempel")
        }
        return ReleaseEvidenceDecision(missing.isEmpty(), missing)
    }
}
