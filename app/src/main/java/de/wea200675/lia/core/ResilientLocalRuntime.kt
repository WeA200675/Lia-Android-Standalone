package de.wea200675.lia.core

import java.io.File

/**
 * Runtime facade that prefers hash-verified native inference and never loses the safe offline path.
 */
class ResilientLocalRuntime(
    private val native: NativeInference,
    private val fallback: ModelRuntime = SafeOfflineRuntime()
) : ModelRuntime {
    private var nativeReady = false
    private var nativeFailureObserved = false

    fun loadModel(spec: ModelSpec, file: File): Boolean {
        native.close()
        nativeReady = false
        nativeFailureObserved = false
        val verified = VerifiedModel.from(spec, file) ?: return false
        nativeReady = runCatching { native.load(verified) }.getOrDefault(false)
        return nativeReady
    }

    override suspend fun generate(prompt: String): Result<String> {
        if (nativeReady) {
            val result = runCatching { native.generate(prompt) }.getOrElse { Result.failure(it) }
            if (result.isSuccess) return result
            nativeReady = false
            nativeFailureObserved = true
        }
        return fallback.generate(prompt)
    }

    /** Returns and clears the flag so a supervisor can perform one bounded recovery action. */
    fun consumeNativeFailure(): Boolean {
        val observed = nativeFailureObserved
        nativeFailureObserved = false
        return observed
    }

    override fun isReady(): Boolean = nativeReady || fallback.isReady()

    fun isNativeReady(): Boolean = nativeReady

    fun nativeState(): NativeRuntimeState = when {
        nativeReady -> NativeRuntimeState.READY
        nativeFailureObserved -> NativeRuntimeState.UNAVAILABLE
        else -> NativeRuntimeState.UNINITIALIZED
    }

    fun close() {
        native.close()
        nativeReady = false
    }
}
