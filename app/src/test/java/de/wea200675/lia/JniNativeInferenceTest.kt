package de.wea200675.lia

import de.wea200675.lia.core.JniNativeInference
import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.ModelVerifier
import de.wea200675.lia.core.NativeInferenceBridge
import de.wea200675.lia.core.VerifiedModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class JniNativeInferenceTest {
    @Test fun usesAllVisibleLogicalThreadsAndDelegatesGeneration() {
        val file = fixture()
        var usedThreads = 0
        var usedPath = ""
        val bridge = object : NativeInferenceBridge {
            override fun load(modelPath: String, modelId: String, threads: Int): Boolean {
                usedPath = modelPath
                usedThreads = threads
                return true
            }
            override fun generate(prompt: String, maxTokens: Int) = "Antwort"
            override fun close() {}
        }
        val adapter = JniNativeInference(bridge) { 16 }
        try {
            assertTrue(adapter.load(verified(file)))
            assertEquals(16, usedThreads)
            assertEquals(file.absolutePath, usedPath)
            assertEquals("Antwort", adapter.generate("Hallo", 256).getOrThrow())
        } finally {
            adapter.close()
            file.delete()
        }
    }

    @Test fun unavailableBridgeFailsClosed() {
        val file = fixture()
        val adapter = JniNativeInference(object : NativeInferenceBridge {
            override fun load(modelPath: String, modelId: String, threads: Int) = false
            override fun generate(prompt: String, maxTokens: Int) = "should not run"
            override fun close() {}
        })
        try {
            assertFalse(adapter.load(verified(file)))
            assertTrue(adapter.generate("Hallo", 256).isFailure)
        } finally {
            file.delete()
        }
    }

    private fun fixture(): File = File.createTempFile("lia-jni-", ".gguf").apply {
        writeBytes("native fixture".toByteArray())
    }

    private fun verified(file: File): VerifiedModel {
        val spec = ModelSpec("fixture", file.name, ModelVerifier.sha256(file), 512)
        return requireNotNull(VerifiedModel.from(spec, file))
    }
}
