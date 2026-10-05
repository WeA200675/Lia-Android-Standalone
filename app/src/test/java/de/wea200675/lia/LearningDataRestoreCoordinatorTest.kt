package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.*
import org.junit.Test

class LearningDataRestoreCoordinatorTest {
    private class MemorySecureStore : SecureStore {
        val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value.copyOf() }
        override fun get(key: String): ByteArray? = values[key]?.copyOf()
        override fun delete(key: String) { values.remove(key) }
    }

    private class Participant(initial: String) : LearningDataBackupParticipant {
        var state = initial
        var failuresRemaining = 0
        override fun exportBackupSnapshot() = state.toByteArray()
        override fun validateBackupSnapshot(snapshot: ByteArray) = snapshot.isNotEmpty()
        override fun restoreBackupSnapshot(snapshot: ByteArray): Boolean {
            if (failuresRemaining > 0) {
                failuresRemaining--
                return false
            }
            state = snapshot.toString(Charsets.UTF_8)
            return true
        }
    }

    private fun coordinator(journal: MemorySecureStore, profile: Participant, knowledge: Participant) =
        LearningDataRestoreCoordinator(journal, profile, knowledge)

    private fun snapshot(profile: String, knowledge: String) =
        LearningDataBackup.Snapshot(profile.toByteArray(), knowledge.toByteArray())

    @Test fun successfulRestoreReplacesBothComponentsAndClearsJournal() {
        val journal = MemorySecureStore()
        val profile = Participant("old-profile")
        val knowledge = Participant("old-knowledge")
        assertEquals(
            LearningDataRestoreStatus.RESTORED,
            coordinator(journal, profile, knowledge).restore(snapshot("new-profile", "new-knowledge"))
        )
        assertEquals("new-profile", profile.state)
        assertEquals("new-knowledge", knowledge.state)
        assertTrue(journal.values.isEmpty())
    }

    @Test fun secondComponentFailureRollsBackTheFirstAndClearsJournal() {
        val journal = MemorySecureStore()
        val profile = Participant("old-profile")
        val knowledge = Participant("old-knowledge").apply { failuresRemaining = 1 }
        assertEquals(
            LearningDataRestoreStatus.ROLLED_BACK,
            coordinator(journal, profile, knowledge).restore(snapshot("new-profile", "new-knowledge"))
        )
        assertEquals("old-profile", profile.state)
        assertEquals("old-knowledge", knowledge.state)
        assertTrue(journal.values.isEmpty())
    }

    @Test fun failedRollbackKeepsJournalAndNextStartupRepairsIdempotently() {
        val journal = MemorySecureStore()
        val profile = Participant("old-profile")
        val knowledge = Participant("old-knowledge").apply { failuresRemaining = 2 }
        val first = coordinator(journal, profile, knowledge)
        assertEquals(
            LearningDataRestoreStatus.RECOVERY_REQUIRED,
            first.restore(snapshot("new-profile", "new-knowledge"))
        )
        assertTrue(journal.values.isNotEmpty())

        knowledge.failuresRemaining = 0
        assertEquals(
            LearningDataRestoreStatus.RECOVERED_PREVIOUS_STATE,
            coordinator(journal, profile, knowledge).recoverPending()
        )
        assertEquals("old-profile", profile.state)
        assertEquals("old-knowledge", knowledge.state)
        assertTrue(journal.values.isEmpty())
        assertEquals(LearningDataRestoreStatus.CLEAN, coordinator(journal, profile, knowledge).recoverPending())
    }

    @Test fun invalidInputDoesNotWriteOrMutateEitherComponent() {
        val journal = MemorySecureStore()
        val profile = Participant("old-profile")
        val knowledge = Participant("old-knowledge")
        assertEquals(
            LearningDataRestoreStatus.INVALID_BACKUP,
            coordinator(journal, profile, knowledge).restore(snapshot("", "new-knowledge"))
        )
        assertEquals("old-profile", profile.state)
        assertEquals("old-knowledge", knowledge.state)
        assertTrue(journal.values.isEmpty())
    }
}
