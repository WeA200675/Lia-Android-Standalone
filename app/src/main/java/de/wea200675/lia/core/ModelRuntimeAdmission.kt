package de.wea200675.lia.core

object ModelRuntimeAdmission {
    fun canStart(report: ModelStorageReport): Boolean =
        report.health != ModelStorageHealth.UNAVAILABLE

    fun explanation(report: ModelStorageReport): String = when (report.health) {
        ModelStorageHealth.HEALTHY -> "Lokale KI kann gestartet werden."
        ModelStorageHealth.LOW_SPACE -> "Lokale KI kann gestartet werden; Speicher ist knapp."
        ModelStorageHealth.UNAVAILABLE -> "Lokale KI bleibt offline, weil der private Modellspeicher nicht verfügbar ist."
    }
}
