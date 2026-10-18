package de.wea200675.lia.core

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Runtime for one explicitly installed model with a safe offline fallback. */
class LocalModelRuntime(
    private val native: NativeInference = JniNativeInference(SystemJniInferenceBridge()),
    private val fallback: ModelRuntime = SafeOfflineRuntime()
) : ModelRuntime {
    @Volatile private var ready = false
    @Volatile private var closed = false

    @Synchronized fun load(spec: ModelSpec, file: File): Boolean {
        if (closed) return false
        native.close()
        ready = false
        val verified = VerifiedModel.from(spec, file) ?: return false
        ready = runCatching { native.load(verified) }.getOrDefault(false)
        return ready
    }

    override suspend fun generate(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        if (!ready) return@withContext fallback.generate(prompt)
        val boundedPrompt = prompt.take(8000)
        val result = runCatching { native.generate(boundedPrompt, 256) }
            .getOrElse { Result.failure(it) }
        if (result.isSuccess && !result.getOrNull().isNullOrBlank()) return@withContext result
        ready = false
        native.close()
        fallback.generate(prompt)
    }

    override fun isReady(): Boolean = ready || fallback.isReady()
    fun isNativeReady(): Boolean = ready

    @Synchronized fun close() {
        closed = true
        native.close()
        ready = false
    }
}

/** Kept injectable for callers that already hold app-private model storage. */
class LocalModelRuntimeFactory(private val modelDirectory: File) {
    fun create(context: Context): LocalModelRuntime {
        val runtime = LocalModelRuntime()
        val entry = ModelCatalog.entries.first()
        val model = ModelInstaller(context, modelDirectory).installedFile(entry) ?: return runtime
        runtime.load(entry.spec(), model)
        return runtime
    }
}
