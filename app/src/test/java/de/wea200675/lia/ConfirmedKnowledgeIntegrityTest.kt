package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.*
import org.junit.Test

class ConfirmedKnowledgeIntegrityTest {
    private class MemoryStore(var value: ByteArray? = null) : SecureStore {
        override fun put(key: String, value: ByteArray) { this.value = value }
        override fun get(key: String): ByteArray? = value
        override fun delete(key: String) { value = null }
    }

    @Test fun keystoreTamperFailureIsNeverHiddenAsEmptyKnowledge() {
        val store = object : SecureStore {
            override fun put(key: String, value: ByteArray) = Unit
            override fun get(key: String): ByteArray? = throw SecurityException("modified ciphertext")
            override fun delete(key: String) = Unit
        }

        assertThrows(SecurityException::class.java) {
            ConfirmedKnowledgeRepository(store).all()
        }
    }

    @Test fun malformedPlaintextIsRemovedWithoutARecoveryLoop() {
        val store = MemoryStore("not-a-valid-record".toByteArray())
        val repository = ConfirmedKnowledgeRepository(store)

        assertTrue(repository.all().isEmpty())
        assertNull(store.value)
        assertTrue(repository.all().isEmpty())
    }

    @Test fun validRecordsSurviveWhileCorruptLinesAreQuarantined() {
        val store = MemoryStore()
        val repository = ConfirmedKnowledgeRepository(store)
        assertTrue(repository.saveConfirmed(
            "Ein bestätigter Fakt.",
            listOf("Wikipedia"),
            ConfirmedKnowledgeRepository.fingerprint("Was ist ein Fakt?")
        ))
        store.value = store.value!! + "\nzerstört".toByteArray()

        val recovered = repository.all()

        assertEquals(1, recovered.size)
        assertFalse(String(store.value!!).contains("zerstört"))
    }

    @Test fun unsafeOrOversizedSourceLabelsAreRejected() {
        val repository = ConfirmedKnowledgeRepository(MemoryStore())
        val fingerprint = ConfirmedKnowledgeRepository.fingerprint("Frage")

        assertFalse(repository.saveConfirmed("Ein Fakt.", listOf("X".repeat(121)), fingerprint))
        assertFalse(repository.saveConfirmed("Ein Fakt.", listOf("Wiki\u202Eevil"), fingerprint))
    }
}
