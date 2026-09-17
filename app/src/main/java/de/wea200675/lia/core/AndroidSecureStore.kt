package de.wea200675.lia.core

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

class AndroidSecureStore(private val alias:String="lia_profile_key") : SecureStore {
    private val prefs by lazy { throw UnsupportedOperationException("Inject encrypted preferences in app layer") }
    private fun key(): java.security.Key {
        val ks=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if(!ks.containsAlias(alias)) KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setUserAuthenticationRequired(false).build()); generateKey()
        }
        return ks.getKey(alias,null)
    }
    override fun put(key:String,value:ByteArray) { throw UnsupportedOperationException("Wire to encrypted preferences") }
    override fun get(key:String):ByteArray? = null
    override fun delete(key:String) {}
    fun encrypt(value:ByteArray):ByteArray { val c=Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE,key()); return c.iv+c.doFinal(value) }
    fun decrypt(value:ByteArray):ByteArray { val c=Cipher.getInstance("AES/GCM/NoPadding); c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,value.copyOfRange(0,12))); return c.doFinal(value.copyOfRange(12,value.size)) }
}
