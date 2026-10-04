package de.wea200675.lia.core

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

/** Versioned, encrypted profile store. Only confirmed entries are returned for prompting. */
class EncryptedLearningProfile(context: Context) {
    private val prefs = context.getSharedPreferences("lia_learning_profile", Context.MODE_PRIVATE)
    private val crypto = AndroidSecureStore(context, alias = "lia_profile_key")
    fun add(item: LearningItem) { val all = readAll().toMutableList(); all += item; writeAll(all) }
    fun confirm(index: Int) { val all = readAll().toMutableList(); if (index in all.indices) { all[index] = all[index].copy(confirmed = true); writeAll(all) } }
    fun confirmed(): List<LearningItem> = readAll().filter { it.confirmed }
    fun confirmAll() { writeAll(readAll().map { it.copy(confirmed = true) }) }
    fun deleteAll() { prefs.edit().clear().apply() }

    /** Exports a validated plaintext snapshot; callers must encrypt it before writing anywhere. */
    fun exportBackupSnapshot(): ByteArray {
        val raw = prefs.getString("payload", null) ?: return JSONObject()
            .put("version", 1).put("items", JSONArray()).toString().toByteArray(Charsets.UTF_8)
        val decrypted = crypto.decrypt(Base64.decode(raw, Base64.DEFAULT))
        val items = decodeItems(JSONArray(String(decrypted, Charsets.UTF_8)))
        return JSONObject().put("version", 1).put("items", encodeItems(items))
            .toString().toByteArray(Charsets.UTF_8)
    }

    /** Validates the entire archive payload before replacing the currently stored profile. */
    fun restoreBackupSnapshot(snapshot: ByteArray): Boolean = try {
        require(snapshot.size <= 8 * 1024 * 1024)
        val root = JSONObject(String(snapshot, Charsets.UTF_8))
        require(root.getInt("version") == 1)
        val items = decodeItems(root.getJSONArray("items"))
        writeAll(items)
        true
    } catch (_: Exception) {
        false
    }

    private fun decodeItems(array: JSONArray): List<LearningItem> {
        require(array.length() <= 2000)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val questionId = item.getString("q")
            val answer = item.getString("a")
            val confirmed = item.getBoolean("c")
            val createdAt = item.getLong("t")
            require(questionId.isNotBlank() && questionId.length <= 160)
            require(answer.length in 1..4000 && createdAt >= 0)
            LearningItem(questionId, answer, confirmed, createdAt)
        }
    }

    private fun encodeItems(items: List<LearningItem>): JSONArray = JSONArray().also { array ->
        items.forEach { item ->
            array.put(JSONObject().put("q", item.questionId).put("a", item.answer)
                .put("c", item.confirmed).put("t", item.createdAt))
        }
    }
    private fun readAll(): List<LearningItem> {
        val raw = prefs.getString("payload", null) ?: return emptyList()
        return try {
            val bytes = crypto.decrypt(Base64.decode(raw, Base64.DEFAULT))
            val a = JSONArray(String(bytes, Charsets.UTF_8))
            (0 until a.length()).map { val o = a.getJSONObject(it); LearningItem(o.getString("q"), o.getString("a"), o.getBoolean("c"), o.getLong("t")) }
        } catch (_: Exception) { emptyList() }
    }
    private fun writeAll(items: List<LearningItem>) {
        val a = JSONArray()
        items.forEach { item -> a.put(JSONObject().apply { put("q", item.questionId); put("a", item.answer); put("c", item.confirmed); put("t", item.createdAt) }) }
        val enc = Base64.encodeToString(crypto.encrypt(a.toString().toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
        prefs.edit().putString("payload", enc).apply()
    }
}
