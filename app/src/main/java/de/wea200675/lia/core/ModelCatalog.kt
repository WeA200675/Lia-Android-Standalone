package de.wea200675.lia.core

/** Open model candidate catalog. A candidate is not installed until the user imports it. */
data class ModelCatalogEntry(
    val id: String,
    val displayName: String,
    val fileName: String,
    val publisher: String,
    val license: String,
    val sourceUrl: String,
    val licenseUrl: String,
    val maxRamMb: Int
) {
    fun spec(sha256: String): ModelSpec =
        ModelSpec(id = id, fileName = fileName, sha256 = sha256, maxRamMb = maxRamMb)
}

object ModelCatalog {
    val entries = listOf(
        ModelCatalogEntry(
            id = "qwen3-0.6b-gguf",
            displayName = "Qwen3 0.6B GGUF (Q8_0)",
            fileName = "qwen3-0.6b-q8_0.gguf",
            publisher = "Qwen",
            license = "Apache-2.0",
            sourceUrl = "https://huggingface.co/Qwen/Qwen3-0.6B-GGUF",
            licenseUrl = "https://huggingface.co/Qwen/Qwen3-0.6B-GGUF",
            maxRamMb = 2048
        )
    )
}
