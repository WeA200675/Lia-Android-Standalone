package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ModelStorageReportTest {
    private val internal = File("/data/user/0/lia/files/models")

    @Test fun reportsHealthyInternalStorage() {
        val report = ModelStorageReporter.from(ModelStorageChoice(internal, false), 2L * 1024 * 1024 * 1024, 6L * 1024 * 1024 * 1024, 8L * 1024 * 1024 * 1024)
        assertEquals(ModelStorageHealth.HEALTHY, report.health)
        assertEquals(75, report.freePercent)
        assertTrue(report.userSummary().contains("bereit"))
    }

    @Test fun warnsBeforeLargeModelCanExhaustStorage() {
        val report = ModelStorageReporter.from(ModelStorageChoice(File("/storage/emulated/0/Android/data/lia/files/models"), true), 7L * 1024 * 1024 * 1024, 256L * 1024 * 1024, 7L * 1024 * 1024 * 1024 + 256L * 1024 * 1024)
        assertEquals(ModelStorageHealth.LOW_SPACE, report.health)
        assertTrue(report.usesExternalAppStorage)
        assertTrue(report.userSummary().contains("fast voll"))
    }

    @Test fun clampsInvalidNegativeMeasurementsSafely() {
        val report = ModelStorageReporter.from(ModelStorageChoice(internal, false), -1, -1, -1)
        assertEquals(ModelStorageHealth.UNAVAILABLE, report.health)
        assertEquals(0, report.freePercent)
    }
}
