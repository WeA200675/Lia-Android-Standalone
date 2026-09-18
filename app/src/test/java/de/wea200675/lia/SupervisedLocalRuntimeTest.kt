package de.wea200675.lia

import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.NativeInference
import de.wea200675.lia.core.ResilientLocalRuntime
import de.wea200675.lia.core.RuntimeCoordinator
import de.wea200675.lia.core.SupervisedLocalRuntime
import de.wea200675.lia.core.VerifiedModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class SupervisedLocalRuntimeTest {
    @Test fun nativeFailureUsesFallbackAndConsumesOneRestart() = runBlocking {
        val file = fixture()
        var factories = 0
        val spec = spec(file)
        val supervisor = SupervisedLocalRuntime({
            factories += 1
            ResilientLocalRuntime(FailingNative(), fallback = de.wea200675.lia.core.SafeOfflineRuntime()).also {
                it.loadModel(spec, file)
            }
        }, coordinator())
        try {
            assertTrue(supervisor.generate("Hallo").isSuccess)
            assertEquals(2, factories)
            assertEquals(2, supervisor.restartBudget())
            assertEquals("BACKOFF", supervisor.state())
        } finally {
            supervisor.close()
            file.delete()
        }
    }

    @Test fun repeatedNativeFailuresEndInBlockedState() = runBlocking {
        val file = fixture()
        val spec = spec(file)
        val supervisor = SupervisedLocalRuntime({
            ResilientLocalRuntime(FailingNative()).also { it.loadModel(spec, file) }
        }, coordinator())
        try {
            repeat(3) { assertTrue(supervisor.generate("Hallo").isSuccess) }
            assertEquals(0, supervisor.restartBudget())
            assertEquals("BLOCKED", supervisor.state())
        } finally {
            supervisor.close()
            file.delete()
        }
    }

    private class FailingNative : NativeInference {
        override fun load(model: VerifiedModel) = true
        override fun generate(prompt: String, maxTokens: Int) = Result.failure<String>(IllegalStateException("native failure"))
        override fun close() {}
    }

    private fun coordinator() = RuntimeCoordinator(
        ModelSpec("primary", "primary.gguf", "0".repeat(64), 2048),
        ModelSpec("recovery", "recovery.gguf", "1".repeat(64), 1024),
        physicalCores = 4,
        logicalThreads = 8
    )

    private fun fixture(): File = File.createTempFile("lia-supervisor-", ".gguf").apply {
        writeText("fixture")
    }

    private fun spec(file: File): ModelSpec {
        val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
            .joinToString("") { "%02x".format(it) }
        return ModelSpec("fixture", file.name, digest, 512)
    }
}
