package de.wea200675.lia.core

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Versioned portable backup envelope. The payload is encrypted with a key derived
 * from a user supplied passphrase; Android Keystore keys are never exported.
 */
object EncryptedBackupArchive {
    private val magic = byteArrayOf(0x4c, 0x49, 0x41, 0x42, 0x4b, 0x50, 0x01, 0x00) // LIABKP v1
    private const val ITERATIONS = 210_000
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private const val KEY_BITS = 256
    const val MAX_PAYLOAD_BYTES = 16 * 1024 * 1024
    private const val HEADER_BYTES = 8 + 4 + SALT_BYTES + IV_BYTES

    fun encrypt(payload: ByteArray, passphrase: CharArray): ByteArray {
        require(payload.size <= MAX_PAYLOAD_BYTES) { "Backup payload is too large." }
        requirePassphrase(passphrase)
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val iv = ByteArray(IV_BYTES).also(SecureRandom()::nextBytes)
        val cipherText = crypt(Cipher.ENCRYPT_MODE, payload, passphrase, salt, iv)
        return ByteBuffer.allocate(HEADER_BYTES + cipherText.size)
            .put(magic).putInt(ITERATIONS).put(salt).put(iv).put(cipherText).array()
    }

    fun decrypt(archive: ByteArray, passphrase: CharArray): ByteArray {
        requirePassphrase(passphrase)
        require(archive.size in (HEADER_BYTES + 16)..(HEADER_BYTES + MAX_PAYLOAD_BYTES + 16)) {
            "Backup archive size is invalid."
        }
        val input = ByteBuffer.wrap(archive)
        val header = ByteArray(magic.size).also(input::get)
        require(header.contentEquals(magic)) { "Unsupported backup format." }
        require(input.int == ITERATIONS) { "Unsupported backup key parameters." }
        val salt = ByteArray(SALT_BYTES).also(input::get)
        val iv = ByteArray(IV_BYTES).also(input::get)
        val encrypted = ByteArray(input.remaining()).also(input::get)
        return crypt(Cipher.DECRYPT_MODE, encrypted, passphrase, salt, iv)
    }

    private fun crypt(mode: Int, input: ByteArray, passphrase: CharArray, salt: ByteArray, iv: ByteArray): ByteArray {
        val spec = PBEKeySpec(passphrase, salt, ITERATIONS, KEY_BITS)
        val key = try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        return try {
            Cipher.getInstance("AES/GCM/NoPadding").run {
                init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
                updateAAD(magic)
                doFinal(input)
            }
        } finally {
            key.fill(0)
        }
    }

    private fun requirePassphrase(passphrase: CharArray) {
        require(passphrase.size in 12..1024) { "Use a passphrase between 12 and 1024 characters." }
    }
}
