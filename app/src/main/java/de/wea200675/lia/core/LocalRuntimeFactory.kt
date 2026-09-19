package de.wea200675.lia.core

import java.io.File

/** Creates the resilient local runtime and loads the first verified model that also initializes successfully. */
class LocalRuntimeFactory(
    private val modelDirectory: File,
    private val nativeFactory: () -> NativeInference = { JniNativeInference(SystemJniInferenceBridge()) },
    private val fallback: ModelRuntime = SafeOfflineRuntime()
) {
    fun create(manifest: ModelManifest): ResilientLocalRuntime =
        create(manifest.primary, manifest.recovery)

    fun create(primary: ModelSpec, recovery: ModelSpec): ResilientLocalRuntime {
        val runtime = ResilientLocalRuntime(nativeFactory(), fallback)
        listOf(primary, recovery)
            .map { it to File(modelDirectory, it.fileName) }
            .filter { (spec, file) -> ModelVerifier.verified(file, spec.sha256) }
            .firstOrNull { (spec, file) -> runtime.loadModel(spec, file) }
        return runtime
    }
}
