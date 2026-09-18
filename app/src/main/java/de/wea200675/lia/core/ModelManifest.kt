package de.wea200675.lia.core

import android.content.Context
import org.json.JSONObject

/** Strict local catalog for the primary and recovery GGUF models. */
data class ModelManifest(val primary: ModelSpec, val recovery: ModelSpec) {
    init { require(primary.fileName != recovery.fileName) { "Primary and recovery models must be different files" } }
}

class ModelManifestLoader(private val context: Context) {
    fun load(assetName: String = "model-manifest.json"): ModelManifest {
        val root = context.assets.open(assetName).bufferedReader().use { JSONObject(it.readText()) }
        require(root.optString("format") == "GGUF") { "Unsupported model manifest format" }
        return ModelManifest(parseSpec(root.getJSONObject("primary")), parseSpec(root.getJSONObject("recovery")))
    }

    private fun parseSpec(json: JSONObject): ModelSpec = ModelSpec(
        id = json.getString("id"),
        fileName = json.getString("file"),
        sha256 = json.getString("sha256"),
        maxRamMb = json.getInt("max_ram_mb")
    )
}
