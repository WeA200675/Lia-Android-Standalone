package de.wea200675.lia.core

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

/** Versioned, encrypted profile store. Only confirmed entries are returned for prompting. */
class EncryptedLearningProfile(context: Context) {
    private val prefs = context.getSharedPreferences("lia_learning_profile", Context.MODE_PRIVATE)
    private val crypto = AndroidSecureStore()
    fun add(item: LearningItem) {
        val all = readAll().toMutableList()
        all += item
        writeAll(all)
    }
    fun confirm(index: Int) {
        val all = readAll().toMutableList()
        if (index in all.indices) { all[index] = all[index].copy(confirmed=true); writeAll(all) }
    }
    fun confirmed(): List<LearningItem> = readAll().filter { it.confirmed }\n    fun confirmAll() { writeAll(readAll().map { it.copy(confirmed=true) }) }
    fun deleteAll() { prefs.edit().clear().apply() }
    private fun readAll(): List<LearningItem> {
        val raw=prefs.getString("payload",null) ?: return emptyList()
        return try {
            val bytes=crypto.decrypt(Base64.decode(raw,Base64.DEFAULT)); val a=JSONArray(String(bytes,Charsets.UTF_8))
            (0 until a.length()).map { val o=a.getJSONObject(it); LearningItem(o.getString("q"),o.getString("a"),o.getBoolean("c"),o.getLong("t")) }
        } catch (_: Exception) { emptyList() }
    }
    private fun writeAll(items: List<LearningItem>) {
        val a=JSONArray(); items.forEach { a.put(JSONObject().apply { put("q",it.questionId); put("a",it.answer); put("c",it.confirmed); put("t",it.createdAt) }) }
        val enc=Base64.encodeToString(crypto.encrypt(a.toString().toByteArray(Charsets.UTF_8)),Base64.NO_WRAP)
        prefs.edit().putString("payload",enc).apply()
    }
}
