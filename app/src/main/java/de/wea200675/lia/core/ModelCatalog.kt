package de.wea200675.lia.core

/** Open model candidate pinned to a publisher file and its published SHA-256 digest. */
data class ModelCatalogEntry(
    val id: String,
    val displayName: String,
    val fileName: String,
    val publisher: String,
    val license: String,
    val sourceUrl: String,
    val licenseUrl: String,
    val maxRamMb: Int,
    val sha256: String
) {
    fun spec(): ModelSpec =
        ModelSpec(id = id, fileName = fileName, sha256 = sha256, maxRamMb = maxRamMb)
}

object ModelCatalog {
    val entries = listOf(
        ModelCatalogEntry(
            id = "qwen3-0.6b-q8_0",
            displayName = "Qwen3 0.6B GGUF (Q8_0)",
            fileName = "Qwen3-0.6B-Q8_0.gguf",
            publisher = "Qwen",
            license = "Apache-2.0",
            sourceUrl = "https://huggingface.co/Qwen/Qwen3-0.6B-GGUF/blob/main/Qwen3-0.6B-Q8_0.gguf",
            licenseUrl = "https://huggingface.co/Qwen/Qwen3-0.6B-GGUF",
            maxRamMb = 2048,
            sha256 = "9465e63a22add5354d9bb4b99e90117043c7124007664907259bd16d043bb031"
        )
    )
}
