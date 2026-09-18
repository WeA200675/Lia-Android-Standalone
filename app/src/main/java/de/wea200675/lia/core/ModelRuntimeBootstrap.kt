package de.wea200675.lia.core

import android.content.Context
import java.io.File

/** Safe app-start boundary for manifest loading, verified model selection and offline fallback. */
class ModelRuntimeBootstrap(
    private val context: Context,
    private val modelDirectory: File = File(context.filesDir, "models")
) {
    fun create(): ResilientLocalRuntime {
        val fallback = ResilientLocalRuntime(UnavailableNativeInference())
        return runCatching {
            val manifest = ModelManifestLoader(context).load()
            LocalRuntimeFactory(modelDirectory).create(manifest)
        }.getOrElse { fallback }
    }

    fun createSupervised(): SupervisedLocalRuntime {
        val primary = ModelSpec("bootstrap-primary", "bootstrap-primary.gguf", "0".repeat(64), 512)
        val recovery = ModelSpec("bootstrap-recovery", "bootstrap-recovery.gguf", "1".repeat(64), 512)
        val budget = RestartBudgetStore(AndroidSecureStore(context)).load()
        val coordinator = RuntimeCoordinator(primary, recovery, maxRestarts = budget)
        return SupervisedLocalRuntime({
            runCatching {
                val manifest = ModelManifestLoader(context).load()
                LocalRuntimeFactory(modelDirectory).create(manifest)
            }.getOrElse { ResilientLocalRuntime(UnavailableNativeInference()) }
        }, coordinator)
    }
}
