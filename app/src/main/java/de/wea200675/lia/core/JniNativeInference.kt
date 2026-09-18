package de.wea200675.lia.core

/** Small injectable boundary around the eventual llama.cpp/LiteRT JNI implementation. */
interface NativeInferenceBridge {
    fun load(modelPath: String, modelId: String, threads: Int): Boolean
    fun generate(prompt: String, maxTokens: Int): String
    fun close()
}

/**
 * Native adapter that uses every Android-visible logical CPU by default.
 * If the .so library or a JNI symbol is unavailable, callers receive a safe failure.
 */
class JniNativeInference(
    private val bridge: NativeInferenceBridge,
    private val logicalThreads: () -> Int = { Runtime.getRuntime().availableProcessors() }
) : NativeInference {
    private var loaded = false

    override fun load(model: VerifiedModel): Boolean {
        close()
        val threads = logicalThreads().coerceAtLeast(1)
        loaded = runCatching { bridge.load(model.file.absolutePath, model.spec.id, threads) }
            .getOrDefault(false)
        return loaded
    }

    override fun generate(prompt: String, maxTokens: Int): Result<String> {
        if (!loaded) return Result.failure(IllegalStateException("Native inference is unavailable"))
        return runCatching { bridge.generate(prompt, maxTokens.coerceIn(1, 4096)) }
    }

    override fun close() {
        if (loaded) runCatching { bridge.close() }
        loaded = false
    }
}

/** Production bridge for a packaged library named lia_llama. */
class SystemJniInferenceBridge(
    private val libraryName: String = "lia_llama"
) : NativeInferenceBridge {
    private var libraryLoaded = false

    override fun load(modelPath: String, modelId: String, threads: Int): Boolean = runCatching {
        if (!libraryLoaded) {
            System.loadLibrary(libraryName)
            libraryLoaded = true
        }
        nativeLoad(modelPath, modelId, threads)
    }.getOrDefault(false)

    override fun generate(prompt: String, maxTokens: Int): String = nativeGenerate(prompt, maxTokens)

    override fun close() {
        if (libraryLoaded) runCatching { nativeClose() }
    }

    private external fun nativeLoad(modelPath: String, modelId: String, threads: Int): Boolean
    private external fun nativeGenerate(prompt: String, maxTokens: Int): String
    private external fun nativeClose()
}
