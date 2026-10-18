package de.wea200675.lia.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EncryptedBackupArchiveTest {
    private val passphrase = "correct-horse-battery".toCharArray()

    @Test fun encryptedArchiveRoundTrips() {
        val payload = "confirmed local memories".toByteArray()
        val archive = EncryptedBackupArchive.encrypt(payload, passphrase)
        assertArrayEquals(payload, EncryptedBackupArchive.decrypt(archive, passphrase))
        org.junit.Assert.assertNotEquals(payload.toList(), archive.toList())
    }

    @Test fun wrongPassphraseIsRejected() {
        val archive = EncryptedBackupArchive.encrypt(byteArrayOf(1, 2, 3), passphrase)
        assertThrows(Exception::class.java) {
            EncryptedBackupArchive.decrypt(archive, "wrong-passphrase".toCharArray())
        }
    }

    @Test fun tamperingIsRejected() {
        val archive = EncryptedBackupArchive.encrypt(byteArrayOf(1, 2, 3), passphrase)
        archive[archive.lastIndex] = (archive.last().toInt() xor 1).toByte()
        assertThrows(Exception::class.java) {
            EncryptedBackupArchive.decrypt(archive, passphrase)
        }
    }

    @Test fun shortPassphrasesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            EncryptedBackupArchive.encrypt(byteArrayOf(1), "short".toCharArray())
        }
    }
}
