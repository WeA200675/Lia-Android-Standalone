package de.wea200675.lia

import de.wea200675.lia.core.LocalRuntimeFactory
import de.wea200675.lia.core.ModelManifest
import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.NativeInference
import de.wea200675.lia.core.VerifiedModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ManifestRuntimeWiringTest {
    @Test fun factoryAcceptsValidatedManifestAsSingleInput() {
        val directory = createTempDir("lia-manifest-runtime-")
        val model = File(directory, "primary.gguf").apply { writeText("primary") }
        val primary = ModelSpec("primary", model.name, sha256(model), 2048)
        val recovery = ModelSpec("recovery", "recovery.gguf", "b".repeat(64), 1024)
        val manifest = ModelManifest(primary, recovery)
        var loaded = ""
        val runtime = LocalRuntimeFactory(directory, nativeFactory = {
            object : NativeInference {
                override fun load(model: VerifiedModel): Boolean { loaded = model.spec.id; return true }
                override fun generate(prompt: String, maxTokens: Int) = Result.success("ok")
                override fun close() {}
            }
        }).create(manifest)
        try {
            assertTrue(runtime.isReady())
            assertEquals("primary", loaded)
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun sha256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        digest.update(file.readBytes())
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
