package de.wea200675.lia.core

/** Snapshot/restore boundary for one encrypted learning-data component. */
interface LearningDataBackupParticipant {
    fun exportBackupSnapshot(): ByteArray
    fun validateBackupSnapshot(snapshot: ByteArray): Boolean
    fun restoreBackupSnapshot(snapshot: ByteArray): Boolean
}

enum class LearningDataRestoreStatus {
    CLEAN,
    RESTORED,
    RECOVERED_PREVIOUS_STATE,
    INVALID_BACKUP,
    ROLLED_BACK,
    FAILED_WITHOUT_CHANGES,
    RECOVERY_REQUIRED
}

/**
 * Two-component write-ahead journal for restore operations.
 *
 * The journal contains the pre-restore snapshot and is stored through SecureStore
 * (Android Keystore encryption). If either replacement fails or the app process
 * dies between component writes, startup replays the old snapshot before exposing
 * learning data. Replaying is idempotent.
 */
class LearningDataRestoreCoordinator(
    private val journalStore: SecureStore,
    private val profile: LearningDataBackupParticipant,
    private val confirmedKnowledge: LearningDataBackupParticipant,
    private val journalKey: String = DEFAULT_JOURNAL_KEY
) {
    fun restore(snapshot: LearningDataBackup.Snapshot): LearningDataRestoreStatus {
        if (!isValid(snapshot)) return LearningDataRestoreStatus.INVALID_BACKUP
        if (recoverPending() == LearningDataRestoreStatus.RECOVERY_REQUIRED) {
            return LearningDataRestoreStatus.RECOVERY_REQUIRED
        }

        val previous = try {
            LearningDataBackup.Snapshot(
                profile.exportBackupSnapshot(),
                confirmedKnowledge.exportBackupSnapshot()
            )
        } catch (_: Exception) {
            return LearningDataRestoreStatus.FAILED_WITHOUT_CHANGES
        }
        if (!isValid(previous)) return LearningDataRestoreStatus.FAILED_WITHOUT_CHANGES

        val journal = try {
            LearningDataBackup.encode(previous.profile, previous.confirmedKnowledge)
        } catch (_: Exception) {
            return LearningDataRestoreStatus.FAILED_WITHOUT_CHANGES
        }
        try {
            journalStore.put(journalKey, journal)
        } catch (_: Exception) {
            return LearningDataRestoreStatus.FAILED_WITHOUT_CHANGES
        }

        return try {
            apply(snapshot)
            journalStore.delete(journalKey)
            LearningDataRestoreStatus.RESTORED
        } catch (_: Exception) {
            try {
                apply(previous)
                journalStore.delete(journalKey)
                LearningDataRestoreStatus.ROLLED_BACK
            } catch (_: Exception) {
                // Keep the encrypted journal so the next app start can repair again.
                LearningDataRestoreStatus.RECOVERY_REQUIRED
            }
        }
    }

    /** Replays a pending pre-restore snapshot. Safe to call on every app startup. */
    fun recoverPending(): LearningDataRestoreStatus {
        val raw = try {
            journalStore.get(journalKey) ?: return LearningDataRestoreStatus.CLEAN
        } catch (_: Exception) {
            return LearningDataRestoreStatus.RECOVERY_REQUIRED
        }
        return try {
            val previous = LearningDataBackup.decode(raw)
            if (!isValid(previous)) return LearningDataRestoreStatus.RECOVERY_REQUIRED
            apply(previous)
            journalStore.delete(journalKey)
            LearningDataRestoreStatus.RECOVERED_PREVIOUS_STATE
        } catch (_: Exception) {
            LearningDataRestoreStatus.RECOVERY_REQUIRED
        }
    }

    private fun isValid(snapshot: LearningDataBackup.Snapshot): Boolean =
        runCatching {
            profile.validateBackupSnapshot(snapshot.profile) &&
                confirmedKnowledge.validateBackupSnapshot(snapshot.confirmedKnowledge)
        }.getOrDefault(false)

    private fun apply(snapshot: LearningDataBackup.Snapshot) {
        check(isValid(snapshot)) { "Learning data snapshot failed validation." }
        check(profile.restoreBackupSnapshot(snapshot.profile)) { "Profile restore failed." }
        check(confirmedKnowledge.restoreBackupSnapshot(snapshot.confirmedKnowledge)) {
            "Confirmed knowledge restore failed."
        }
    }

    private companion object {
        const val DEFAULT_JOURNAL_KEY = "lia.learning.restore.journal.v1"
    }
}
