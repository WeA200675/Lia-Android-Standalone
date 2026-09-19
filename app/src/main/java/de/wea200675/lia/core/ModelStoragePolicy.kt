package de.wea200675.lia.core

object ModelStoragePolicy {
    fun canAcceptModel(report: ModelStorageReport, modelBytes: Long): Boolean {
        val required = modelBytes.coerceAtLeast(0L)
        if (report.health == ModelStorageHealth.UNAVAILABLE) return false
        return required <= report.availableBytes
    }

    fun rejectionReason(report: ModelStorageReport, modelBytes: Long): String? {
        if (report.health == ModelStorageHealth.UNAVAILABLE) return "Modellspeicher nicht verfügbar"
        if (modelBytes < 0L) return "Ungültige Modellgröße"
        if (modelBytes > report.availableBytes) {
            return "Nicht genug freier Speicher für dieses geprüfte Modell"
        }
        return null
    }
}
