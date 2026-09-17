package de.wea200675.lia.core

import android.content.Context
import android.util.Base64
import org.json.JSONObject

/** Encrypted persistence for explicit sensitivity preferences. */
class SensitivityStore(context: Context) {
    private val prefs = context.getSharedPreferences("lia_sensitivity", Context.MODE_PRIVATE)
    private val crypto = AndroidSecureStore()
    fun load(): SensitivityProfile {
        val raw = prefs.getString("payload", null) ?: return SensitivityProfile()
        return try {
            val o = JSONObject(String(crypto.decrypt(Base64.decode(raw, Base64.DEFAULT)), Charsets.UTF_8))
            SensitivityProfile(o.optBoolean("slower"), o.optBoolean("gentler"), emptySet(), o.optString("style", "warm"))
        } catch (_: Exception) { SensitivityProfile() }
    }
    fun save(profile: SensitivityProfile) {
        val o = JSONObject().put("slower", profile.slower).put("gentler", profile.gentler).put("style", profile.preferredStyle)
        prefs.edit().putString("payload", Base64.encodeToString(crypto.encrypt(o.toString().toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)).apply()
    }
    fun clear() = prefs.edit().clear().apply()
}
