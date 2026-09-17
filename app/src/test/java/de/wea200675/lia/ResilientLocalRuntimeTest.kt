package de.wea200675.lia

import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.NativeInference
import de.wea200675.lia.core.ResilientLocalRuntime
import org.junit.Assert.assertTrue
import org.junit.Test

class ResilientLocalRuntimeTest {
    @Test fun unavailableNativeKeepsFallbackReady() {
        val runtime = ResilientLocalRuntime(object : NativeInference {
            override fun load(verifiedModel: ModelSpec) = false
            override fun generate(prompt: String, maxTokens: Int) = Result.failure<String>(IllegalStateException())
            override fun close() {}
        })
        assertTrue(runtime.isReady())
    }
}
