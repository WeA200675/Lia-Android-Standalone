package de.wea200675.lia.core

import java.io.File

enum class ModelStorageHealth { HEALTHY, LOW_SPACE, UNAVAILABLE }

data class ModelStorageReport(
    val directory: File,
    val usedBytes: Long,
    val availableBytes: Long,
    val capacityBytes: Long,
    val usesExternalAppStorage: Boolean,
    val health: ModelStorageHealth
) {
    val freePercent: Int
        get() = if (capacityBytes <= 0L) 0 else ((availableBytes * 100L) / capacityBytes).coerceIn(0L, 100L).toInt()

    fun userSummary(): String = when (health) {
        ModelStorageHealth.HEALTHY -> "Modellspeicher bereit (${freePercent}% frei)"
        ModelStorageHealth.LOW_SPACE -> "Modellspeicher fast voll (${freePercent}% frei)"
        ModelStorageHealth.UNAVAILABLE -> "Modellspeicher nicht verfügbar"
    }
}

object ModelStorageReporter {
    const val LOW_SPACE_THRESHOLD_BYTES = 512L * 1024L * 1024L

    fun from(choice: ModelStorageChoice, usedBytes: Long, availableBytes: Long, capacityBytes: Long): ModelStorageReport {
        val safeUsed = usedBytes.coerceAtLeast(0L)
        val safeAvailable = availableBytes.coerceAtLeast(0L)
        val safeCapacity = capacityBytes.coerceAtLeast(safeUsed + safeAvailable)
        val health = when {
            safeCapacity == 0L -> ModelStorageHealth.UNAVAILABLE
            safeAvailable < LOW_SPACE_THRESHOLD_BYTES -> ModelStorageHealth.LOW_SPACE
            else -> ModelStorageHealth.HEALTHY
        }
        return ModelStorageReport(choice.directory, safeUsed, safeAvailable, safeCapacity, choice.usesExternalAppStorage, health)
    }

    fun forDirectory(choice: ModelStorageChoice): ModelStorageReport {
        val stat = android.os.StatFs(choice.directory.absolutePath)
        val blockSize = stat.blockSizeLong
        val available = stat.availableBlocksLong * blockSize
        val capacity = stat.blockCountLong * blockSize
        val used = choice.directory.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return from(choice, used, available, capacity)
    }
}
