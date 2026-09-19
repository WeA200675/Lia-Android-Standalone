package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ModelRuntimeAdmissionTest {
    private val choice = ModelStorageChoice(File("/data/user/0/lia/files/models"), false)

    @Test fun startsWhenStorageIsHealthyOrLow() {
        val healthy = ModelStorageReporter.from(choice, 0, 1024 * 1024 * 1024L, 2 * 1024 * 1024 * 1024L)
        val low = ModelStorageReporter.from(choice, 0, 1, 1024)
        assertTrue(ModelRuntimeAdmission.canStart(healthy))
        assertTrue(ModelRuntimeAdmission.canStart(low))
    }

    @Test fun refusesUnavailableStorageBeforeNativeLoad() {
        val unavailable = ModelStorageReporter.from(choice, 0, 0, 0)
        assertFalse(ModelRuntimeAdmission.canStart(unavailable))
    }
}
