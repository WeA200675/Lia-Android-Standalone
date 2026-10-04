package de.wea200675.lia.core

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Runtime for one explicitly installed model; the normal safe offline runtime remains the fallback. */
class LocalModelRuntime(
    private val native: NativeInference = JniNativeInference(SystemJniInferenceBridge()),
    private val fallback: ModelRuntime = SafeOfflineRuntime()
) : ModelRuntime {
    @Volatile private var ready = false

    fun load(spec: ModelSpec, file: File): Boolean {
        native.close()
        ready = false
        val verified = VerifiedModel.from(spec, file) ?: return false
        ready = runCatching { native.load(verified) }.getOrDefault(false)
        return ready
    }

    override suspend fun generate(prompt: String): Result<String> {
        if (!ready) return fallback.generate(prompt)
        val boundedPrompt = prompt.take(8000)
        val result = runCatching { native.generate(boundedPrompt, 256) }
            .getOrElse { Result.failure(it) }
        if (result.isSuccess && !result.getOrNull().isNullOrBlank()) return result
        ready = false
        native.close()
        return fallback.generate(prompt)
    }

    override fun isReady(): Boolean = ready || fallback.isReady()
    fun isNativeReady(): Boolean = ready

    fun close() {
        native.close()
        ready = false
    }
}

class LocalModelRuntimeFactory(private val modelDirectory: File) {
    fun create(context: android.content.Context): LocalModelRuntime {
        val runtime = LocalModelRuntime()
        val entry = ModelCatalog.entries.first()
        val installer = ModelInstaller(context, modelDirectory)
        val model = installer.installedFile(entry) ?: return runtime
        val sha = context.getSharedPreferences("lia_models", android.content.Context.MODE_PRIVATE)
            .getString("active_model_sha256", null) ?: return runtime
        runtime.load(entry.spec(sha), model)
        return runtime
    }
}
