package de.wea200675.lia

import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.ModelVerifier
import de.wea200675.lia.core.NativeInference
import de.wea200675.lia.core.ResilientLocalRuntime
import de.wea200675.lia.core.VerifiedModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ResilientLocalRuntimeTest {
    @Test fun unavailableNativeKeepsFallbackReady() {
        val runtime = ResilientLocalRuntime(object : NativeInference {
            override fun load(model: VerifiedModel) = false
            override fun generate(prompt: String, maxTokens: Int) = Result.failure<String>(IllegalStateException())
            override fun close() {}
        })
        assertTrue(runtime.isReady())
    }

    @Test fun hashMismatchNeverReachesNativeAdapter() {
        val modelFile = temporaryModel()
        var loadCalled = false
        val runtime = ResilientLocalRuntime(object : NativeInference {
            override fun load(model: VerifiedModel): Boolean { loadCalled = true; return true }
            override fun generate(prompt: String, maxTokens: Int) = Result.success("ok")
            override fun close() {}
        })
        try {
            val spec = ModelSpec("primary", modelFile.name, "0".repeat(64), 4096)
            assertFalse(runtime.loadModel(spec, modelFile))
            assertFalse(loadCalled)
            assertTrue(runtime.isReady())
        } finally {
            modelFile.delete()
        }
    }

    @Test fun validHashPassesVerifiedFileToNativeAdapter() {
        val modelFile = temporaryModel()
        var received: File? = null
        val runtime = ResilientLocalRuntime(object : NativeInference {
            override fun load(model: VerifiedModel): Boolean { received = model.file; return true }
            override fun generate(prompt: String, maxTokens: Int) = Result.success("ok")
            override fun close() {}
        })
        try {
            val spec = ModelSpec("primary", modelFile.name, ModelVerifier.sha256(modelFile), 4096)
            assertTrue(runtime.loadModel(spec, modelFile))
            assertTrue(received == modelFile)
        } finally {
            modelFile.delete()
        }
    }

    private fun temporaryModel(): File = File.createTempFile("lia-model-", ".gguf").apply {
        writeBytes("verified local model fixture".toByteArray())
    }
}
