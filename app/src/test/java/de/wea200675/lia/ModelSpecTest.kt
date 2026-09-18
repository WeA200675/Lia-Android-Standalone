package de.wea200675.lia

import de.wea200675.lia.core.ModelSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ModelSpecTest {
    @Test fun acceptsBoundedLocalGgufSpecification() {
        val spec = ModelSpec("lia-primary-3b", "lia-primary-q4.gguf", "a".repeat(64), 4096)
        assertEquals("lia-primary-q4.gguf", spec.fileName)
    }

    @Test fun rejectsPathTraversal() {
        assertThrows(IllegalArgumentException::class.java) {
            ModelSpec("primary", "../outside.gguf", "a".repeat(64), 4096)
        }
    }

    @Test fun rejectsPlaceholderOrMalformedHash() {
        assertThrows(IllegalArgumentException::class.java) {
            ModelSpec("primary", "primary.gguf", "REPLACE_WITH_RELEASE_HASH", 4096)
        }
    }

    @Test fun rejectsUnboundedRamRequirement() {
        assertThrows(IllegalArgumentException::class.java) {
            ModelSpec("primary", "primary.gguf", "a".repeat(64), Int.MAX_VALUE)
        }
    }
}
