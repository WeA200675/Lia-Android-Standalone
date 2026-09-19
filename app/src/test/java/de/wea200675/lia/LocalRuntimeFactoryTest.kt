package de.wea200675.lia

import de.wea200675.lia.core.LocalRuntimeFactory
import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.ModelVerifier
import de.wea200675.lia.core.NativeInference
import de.wea200675.lia.core.VerifiedModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalRuntimeFactoryTest {
    @Test fun selectsVerifiedPrimaryBeforeRecovery() {
        val directory = directoryWith("primary.gguf", "primary")
        var loadedId: String? = null
        val primary = spec("primary", "primary.gguf", directory)
        val recovery = ModelSpec("recovery", "recovery.gguf", "b".repeat(64), 1024)
        val runtime = LocalRuntimeFactory(directory, nativeFactory = {
            fakeNative { loadedId = it.spec.id }
        }).create(primary, recovery)
        try {
            assertTrue(runtime.isReady())
            assertEquals("primary", loadedId)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun fallsBackToVerifiedRecoveryWhenPrimaryMissing() {
        val directory = directoryWith("recovery.gguf", "recovery")
        var loadedId: String? = null
        val primary = ModelSpec("primary", "primary.gguf", "a".repeat(64), 2048)
        val recovery = spec("recovery", "recovery.gguf", directory)
        val runtime = LocalRuntimeFactory(directory, nativeFactory = {
            fakeNative { loadedId = it.spec.id }
        }).create(primary, recovery)
        try {
            assertTrue(runtime.isReady())
            assertEquals("recovery", loadedId)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun noVerifiedModelLeavesNativeAdapterUnused() {
        val directory = directoryWith("unrelated.txt", "not a model")
        var loadCalled = false
        val primary = ModelSpec("primary", "primary.gguf", "a".repeat(64), 2048)
        val recovery = ModelSpec("recovery", "recovery.gguf", "b".repeat(64), 1024)
        val runtime = LocalRuntimeFactory(directory, nativeFactory = {
            fakeNative { loadCalled = true }
        }).create(primary, recovery)
        try {
            assertTrue(runtime.isReady())
            assertFalse(loadCalled)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun triesRecoveryWhenVerifiedPrimaryCannotInitialize() {
        val directory = directoryWith("primary.gguf", "primary")
        directoryWithIn(directory, "recovery.gguf", "recovery")
        val loadedIds = mutableListOf<String>()
        val primary = spec("primary", "primary.gguf", directory)
        val recovery = spec("recovery", "recovery.gguf", directory)
        val runtime = LocalRuntimeFactory(directory, nativeFactory = {
            fakeNative { model ->
                loadedIds += model.spec.id
                model.spec.id == "recovery"
            }
        }).create(primary, recovery)
        try {
            assertTrue(runtime.isReady())
            assertEquals(listOf("primary", "recovery"), loadedIds)
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun fakeNative(onLoad: (VerifiedModel) -> Unit) = object : NativeInference {
        override fun load(model: VerifiedModel): Boolean { onLoad(model); return true }
        override fun generate(prompt: String, maxTokens: Int) = Result.success("ok")
        override fun close() {}
    }

    private fun spec(id: String, fileName: String, directory: File) =
        ModelSpec(id, fileName, ModelVerifier.sha256(File(directory, fileName)), 2048)

    private fun directoryWith(name: String, content: String): File = createTempDir("lia-runtime-").apply {
        File(this, name).writeText(content)
    }

    private fun directoryWithIn(directory: File, name: String, content: String) {
        File(directory, name).writeText(content)
    }
}
