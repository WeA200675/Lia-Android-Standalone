package de.wea200675.lia.core

object ModelRuntimeAdmission {
    fun canStart(report: ModelStorageReport): Boolean =
        report.health != ModelStorageHealth.UNAVAILABLE
}
