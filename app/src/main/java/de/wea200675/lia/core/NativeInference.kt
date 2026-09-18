package de.wea200675.lia.core

import java.io.File

/** A model/file pair that can only be created after its SHA-256 digest is verified. */
class VerifiedModel private constructor(
    val spec: ModelSpec,
    val file: File
) {
    companion object {
        fun from(spec: ModelSpec, file: File): VerifiedModel? =
            if (ModelVerifier.verified(file, spec.sha256)) VerifiedModel(spec, file) else null
    }
}

/** Adapter boundary for a future native Android GGUF engine (LiteRT/llama.cpp JNI). */
interface NativeInference {
    fun load(model: VerifiedModel): Boolean
    fun generate(prompt: String, maxTokens: Int = 256): Result<String>
    fun close()
}

class UnavailableNativeInference : NativeInference {
    override fun load(model: VerifiedModel): Boolean = false
    override fun generate(prompt: String, maxTokens: Int): Result<String> =
        Result.failure(IllegalStateException("Native Android inference adapter not installed"))
    override fun close() {}
}
