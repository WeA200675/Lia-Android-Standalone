package de.wea200675.lia

import de.wea200675.lia.core.LocalModelRuntime
import de.wea200675.lia.core.ModelRuntime
import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.ModelVerifier
import de.wea200675.lia.core.NativeInference
import de.wea200675.lia.core.SafeOfflineRuntime
import de.wea200675.lia.core.VerifiedModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalModelRuntimeTest {
    @Test fun hashMismatchNeverLoadsNativeModelAndUsesOfflineFallback() = runBlocking {
        val file = modelFile()
        var nativeLoadCalled = false
        val runtime = LocalModelRuntime(native = fakeNative(
            onLoad = { nativeLoadCalled = true; true },
            onGenerate = { "native" }
        ))
        try {
            val invalid = ModelSpec("local", file.name, "0".repeat(64), 2048)
            assertFalse(runtime.load(invalid, file))
            assertFalse(nativeLoadCalled)
            assertEquals(SafeOfflineRuntime.RESPONSE, runtime.generate("Hallo").getOrThrow())
            assertFalse(runtime.isNativeReady())
        } finally {
            runtime.close()
            file.delete()
        }
    }

    @Test fun verifiedModelUsesNativeInferenceOffCallerThread() = runBlocking {
        val file = modelFile()
        var loadedFile: File? = null
        var inferenceThread: Thread? = null
        val runtime = LocalModelRuntime(native = fakeNative(
            onLoad = { loadedFile = it.file; true },
            onGenerate = { inferenceThread = Thread.currentThread(); "Hallo, ich bin Lia." }
        ))
        try {
            val spec = ModelSpec("local", file.name, ModelVerifier.sha256(file), 2048)
            assertTrue(runtime.load(spec, file))
            assertEquals(file, loadedFile)
            assertEquals("Hallo, ich bin Lia.", runtime.generate("Hallo").getOrThrow())
            assertTrue(runtime.isNativeReady())
            assertTrue(inferenceThread !== Thread.currentThread())
        } finally {
            runtime.close()
            file.delete()
        }
    }

    @Test fun nativeGenerationFailureClosesRuntimeAndUsesFallback() = runBlocking {
        val file = modelFile()
        var closeCount = 0
        val runtime = LocalModelRuntime(native = object : NativeInference {
            override fun load(model: VerifiedModel) = true
            override fun generate(prompt: String, maxTokens: Int): Result<String> =
                Result.failure(IllegalStateException("native failure"))
            override fun close() { closeCount++ }
        })
        try {
            val spec = ModelSpec("local", file.name, ModelVerifier.sha256(file), 2048)
            assertTrue(runtime.load(spec, file))
            assertEquals(SafeOfflineRuntime.RESPONSE, runtime.generate("Hallo").getOrThrow())
            assertFalse(runtime.isNativeReady())
            assertTrue(closeCount >= 2)
        } finally {
            runtime.close()
            file.delete()
        }
    }

    private fun fakeNative(
        onLoad: (VerifiedModel) -> Boolean,
        onGenerate: (String) -> String
    ) = object : NativeInference {
        override fun load(model: VerifiedModel): Boolean = onLoad(model)
        override fun generate(prompt: String, maxTokens: Int): Result<String> =
            runCatching { onGenerate(prompt) }
        override fun close() {}
    }

    private fun modelFile(): File = File.createTempFile("lia-local-", ".gguf").apply {
        writeText("small deterministic fixture")
    }
}
