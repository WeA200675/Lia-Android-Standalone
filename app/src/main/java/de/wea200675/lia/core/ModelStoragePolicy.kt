package de.wea200675.lia.core

object ModelStoragePolicy {
    fun canAcceptModel(report: ModelStorageReport, modelBytes: Long): Boolean {
        if (modelBytes < 0L) return false
        if (report.health == ModelStorageHealth.UNAVAILABLE) return false
        return modelBytes <= report.availableBytes
    }

    fun rejectionReason(report: ModelStorageReport, modelBytes: Long): String? {
        if (modelBytes < 0L) return "Ungültige Modellgröße"
        if (report.health == ModelStorageHealth.UNAVAILABLE) return "Modellspeicher nicht verfügbar"
        if (modelBytes > report.availableBytes) {
            return "Nicht genug freier Speicher für dieses geprüfte Modell"
        }
        return null
    }
}
