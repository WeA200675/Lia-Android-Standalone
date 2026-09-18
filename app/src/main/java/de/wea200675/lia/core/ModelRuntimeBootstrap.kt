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
}
