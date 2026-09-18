package de.wea200675.lia.core

import java.io.File

/** Creates the resilient local runtime and loads only the first verified primary/recovery model. */
class LocalRuntimeFactory(
    private val modelDirectory: File,
    private val nativeFactory: () -> NativeInference = { JniNativeInference(SystemJniInferenceBridge()) },
    private val fallback: ModelRuntime = SafeOfflineRuntime()
) {
    fun create(manifest: ModelManifest): ResilientLocalRuntime =
        create(manifest.primary, manifest.recovery)

    fun create(primary: ModelSpec, recovery: ModelSpec): ResilientLocalRuntime {
        val runtime = ResilientLocalRuntime(nativeFactory(), fallback)
        val candidate = listOf(primary, recovery).firstOrNull { spec ->
            ModelVerifier.verified(File(modelDirectory, spec.fileName), spec.sha256)
        }
        if (candidate != null) {
            runtime.loadModel(candidate, File(modelDirectory, candidate.fileName))
        }
        return runtime
    }
}
