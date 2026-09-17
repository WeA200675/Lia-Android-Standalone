package de.wea200675.lia.core

/**
 * Runtime facade that prefers verified native inference and never loses the safe offline path.
 */
class ResilientLocalRuntime(
    private val native: NativeInference,
    private val fallback: ModelRuntime = SafeOfflineRuntime()
) : ModelRuntime {
    private var nativeReady = false

    fun loadVerifiedModel(spec: ModelSpec): Boolean {
        nativeReady = native.load(spec)
        return nativeReady
    }

    override suspend fun generate(prompt: String): Result<String> {
        if (nativeReady) {
            val result = runCatching { native.generate(prompt) }.getOrElse { Result.failure(it) }
            if (result.isSuccess) return result
            nativeReady = false
        }
        return fallback.generate(prompt)
    }

    override fun isReady(): Boolean = nativeReady || fallback.isReady()

    fun close() {
        native.close()
        nativeReady = false
    }
}
