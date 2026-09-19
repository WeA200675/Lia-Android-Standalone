package de.wea200675.lia

import de.wea200675.lia.core.ModelStorageLocator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModelStorageLocatorTest {
    @Test fun keepsModelsInternalWhenThereIsEnoughSpace() {
        val internal = File("/data/user/0/lia/files/models")
        val external = File("/storage/emulated/0/Android/data/lia/files/models")
        val choice = ModelStorageLocator.choose(internal, external, ModelStorageLocator.MIN_INTERNAL_FREE_BYTES, true)
        assertSame(internal, choice.directory)
        assertFalse(choice.usesExternalAppStorage)
    }

    @Test fun usesMountedAppExternalStorageWhenInternalSpaceIsLow() {
        val internal = File("/data/user/0/lia/files/models")
        val external = File("/storage/emulated/0/Android/data/lia/files/models")
        val choice = ModelStorageLocator.choose(internal, external, 512L * 1024L * 1024L, true)
        assertSame(external, choice.directory)
        assertTrue(choice.usesExternalAppStorage)
    }

    @Test fun neverUsesUnavailableExternalStorage() {
        val internal = File("/data/user/0/lia/files/models")
        val external = File("/storage/emulated/0/Android/data/lia/files/models")
        val choice = ModelStorageLocator.choose(internal, external, 512L * 1024L * 1024L, false)
        assertSame(internal, choice.directory)
        assertFalse(choice.usesExternalAppStorage)
    }
}
