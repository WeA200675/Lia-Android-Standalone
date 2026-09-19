package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ModelStoragePolicyTest {
    private val choice = ModelStorageChoice(File("/data/user/0/lia/files/models"), false)

    @Test fun acceptsModelThatFitsAvailableCapacity() {
        val report = ModelStorageReporter.from(choice, 1, 4 * 1024 * 1024, 5 * 1024 * 1024)
        assertTrue(ModelStoragePolicy.canAcceptModel(report, 3 * 1024 * 1024L))
        assertNull(ModelStoragePolicy.rejectionReason(report, 3 * 1024 * 1024L))
    }

    @Test fun rejectsModelLargerThanFreeSpace() {
        val report = ModelStorageReporter.from(choice, 1, 4 * 1024 * 1024, 5 * 1024 * 1024)
        assertFalse(ModelStoragePolicy.canAcceptModel(report, 5 * 1024 * 1024L))
        assertEquals("Nicht genug freier Speicher für dieses geprüfte Modell", ModelStoragePolicy.rejectionReason(report, 5 * 1024 * 1024L))
    }

    @Test fun rejectsInvalidSizeAndUnavailableStorage() {
        val report = ModelStorageReporter.from(choice, 0, 0, 0)
        assertFalse(ModelStoragePolicy.canAcceptModel(report, 1))
        assertEquals("Modellspeicher nicht verfügbar", ModelStoragePolicy.rejectionReason(report, 1))
        val healthy = ModelStorageReporter.from(choice, 0, 1024, 1024)
        assertFalse(ModelStoragePolicy.canAcceptModel(healthy, -1))
        assertEquals("Ungültige Modellgröße", ModelStoragePolicy.rejectionReason(healthy, -1))
    }
}
