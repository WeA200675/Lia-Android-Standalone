package de.wea200675.lia.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/** Bounded container for the user's learning profile and confirmed local knowledge. */
object LearningDataBackup {
    data class Snapshot(val profile: ByteArray, val confirmedKnowledge: ByteArray)

    private val magic = byteArrayOf(0x4c, 0x49, 0x41, 0x44, 0x41, 0x54, 0x41, 0x01)
    private const val MAX_COMPONENT_BYTES = 8 * 1024 * 1024

    fun encode(profile: ByteArray, confirmedKnowledge: ByteArray): ByteArray {
        require(profile.size <= MAX_COMPONENT_BYTES && confirmedKnowledge.size <= MAX_COMPONENT_BYTES)
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.write(magic)
            out.writeInt(profile.size)
            out.write(profile)
            out.writeInt(confirmedKnowledge.size)
            out.write(confirmedKnowledge)
        }
        require(bytes.size() <= EncryptedBackupArchive.MAX_PAYLOAD_BYTES)
        return bytes.toByteArray()
    }

    fun decode(payload: ByteArray): Snapshot {
        require(payload.size <= EncryptedBackupArchive.MAX_PAYLOAD_BYTES)
        DataInputStream(ByteArrayInputStream(payload)).use { input ->
            val header = ByteArray(magic.size)
            input.readFully(header)
            require(header.contentEquals(magic)) { "Unsupported Lia backup payload." }
            val profileLength = input.readInt()
            require(profileLength in 0..MAX_COMPONENT_BYTES)
            val profile = ByteArray(profileLength).also(input::readFully)
            val knowledgeLength = input.readInt()
            require(knowledgeLength in 0..MAX_COMPONENT_BYTES)
            val knowledge = ByteArray(knowledgeLength).also(input::readFully)
            require(input.available() == 0) { "Unexpected trailing backup data." }
            return Snapshot(profile, knowledge)
        }
    }
}
