package de.wea200675.lia.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/** Durable encrypted key/value storage backed by Android Keystore and private preferences. */
class AndroidSecureStore(
    context: Context,
    private val alias: String = "lia_secure_store_key",
    preferencesName: String = "lia_secure_store"
) : SecureStore {
    private val preferences = context.applicationContext
        .getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    private fun key(): java.security.Key {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!keyStore.containsAlias(alias)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(
                    KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setUserAuthenticationRequired(false)
                        .build()
                )
                generateKey()
            }
        }
        return requireNotNull(keyStore.getKey(alias, null)) { "Keystore key unavailable" }
    }

    override fun put(key: String, value: ByteArray) {
        require(key.isNotBlank()) { "Storage key must not be blank" }
        val encoded = Base64.encodeToString(encrypt(value), Base64.NO_WRAP)
        check(preferences.edit().putString(key, encoded).commit()) { "Encrypted value could not be persisted" }
    }

    override fun get(key: String): ByteArray? {
        require(key.isNotBlank()) { "Storage key must not be blank" }
        val encoded = preferences.getString(key, null) ?: return null
        return try {
            decrypt(Base64.decode(encoded, Base64.NO_WRAP))
        } catch (error: Exception) {
            throw SecurityException("Encrypted value is unreadable or was modified", error)
        }
    }

    override fun delete(key: String) {
        require(key.isNotBlank()) { "Storage key must not be blank" }
        check(preferences.edit().remove(key).commit()) { "Encrypted value could not be deleted" }
    }

    internal fun encrypt(value: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return cipher.iv + cipher.doFinal(value)
    }

    internal fun decrypt(value: ByteArray): ByteArray {
        require(value.size > GCM_IV_BYTES) { "Encrypted value is too short" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            key(),
            GCMParameterSpec(128, value.copyOfRange(0, GCM_IV_BYTES))
        )
        return cipher.doFinal(value.copyOfRange(GCM_IV_BYTES, value.size))
    }

    private companion object {
        const val GCM_IV_BYTES = 12
    }
}
