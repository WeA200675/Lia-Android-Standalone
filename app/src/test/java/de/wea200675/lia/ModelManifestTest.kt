package de.wea200675.lia

import de.wea200675.lia.core.ModelManifest
import de.wea200675.lia.core.ModelSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ModelManifestTest {
    private val primary = ModelSpec("primary", "primary.gguf", "a".repeat(64), 4096)
    private val recovery = ModelSpec("recovery", "recovery.gguf", "b".repeat(64), 2048)

    @Test fun acceptsDistinctVerifiedModelSpecs() {
        val manifest = ModelManifest(primary, recovery)
        assertEquals("primary", manifest.primary.id)
        assertEquals("recovery", manifest.recovery.id)
    }

    @Test fun rejectsDuplicateModelFiles() {
        assertThrows(IllegalArgumentException::class.java) {
            ModelManifest(primary, recovery.copy(fileName = primary.fileName))
        }
    }
}
