package de.wea200675.lia.core

data class ModelSpec(
    val id: String,
    val fileName: String,
    val sha256: String,
    val maxRamMb: Int
) {
    init {
        require(id.matches(Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,79}"))) { "Invalid model id" }
        require(fileName.length in 6..128) { "Invalid model filename length" }
        require(fileName.matches(Regex("[A-Za-z0-9][A-Za-z0-9._-]*\\.gguf"))) { "Model must be a local GGUF filename" }
        require(!fileName.contains("..")) { "Model filename must not contain traversal segments" }
        require(sha256.matches(Regex("[0-9a-fA-F]{64}"))) { "Model SHA-256 must contain 64 hexadecimal characters" }
        require(maxRamMb in 256..32768) { "Model RAM requirement is outside supported bounds" }
    }
}

interface ModelRuntime { suspend fun generate(prompt:String):Result<String>; fun isReady():Boolean }
class SafeOfflineRuntime : ModelRuntime {
 companion object { const val RESPONSE = "Ich bin im lokalen Grundmodus. Ich kann dir bei Alltag, Erinnerungen und Fragen helfen." }
 override suspend fun generate(prompt:String):Result<String> = Result.success(RESPONSE)
 override fun isReady()=true
}
