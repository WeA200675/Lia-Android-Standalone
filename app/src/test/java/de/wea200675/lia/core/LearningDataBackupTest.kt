package de.wea200675.lia.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LearningDataBackupTest {
    @Test fun snapshotsRoundTripExactly() {
        val profile = """{"version":1,"items":[]}""".toByteArray()
        val knowledge = "confirmed|knowledge".toByteArray()
        val decoded = LearningDataBackup.decode(LearningDataBackup.encode(profile, knowledge))
        assertArrayEquals(profile, decoded.profile)
        assertArrayEquals(knowledge, decoded.confirmedKnowledge)
    }

    @Test fun malformedAndTrailingPayloadsAreRejected() {
        assertThrows(Exception::class.java) { LearningDataBackup.decode(byteArrayOf(1, 2, 3)) }
        val valid = LearningDataBackup.encode(byteArrayOf(1), byteArrayOf(2))
        assertThrows(IllegalArgumentException::class.java) {
            LearningDataBackup.decode(valid + byteArrayOf(0))
        }
    }
}
