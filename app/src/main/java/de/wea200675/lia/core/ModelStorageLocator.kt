package de.wea200675.lia.core

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File

data class ModelStorageChoice(val directory: File, val usesExternalAppStorage: Boolean)

object ModelStorageLocator {
    const val MIN_INTERNAL_FREE_BYTES = 1L * 1024L * 1024L * 1024L

    fun choose(
        internal: File,
        external: File?,
        internalFreeBytes: Long,
        externalMounted: Boolean
    ): ModelStorageChoice {
        if (internalFreeBytes >= MIN_INTERNAL_FREE_BYTES || external == null || !externalMounted) {
            return ModelStorageChoice(internal, false)
        }
        return ModelStorageChoice(external, true)
    }

    fun forContext(context: Context): ModelStorageChoice {
        val internal = File(context.filesDir, "models")
        val external = context.getExternalFilesDir("models")
        val internalFree = StatFs(context.filesDir.absolutePath).availableBytes
        val mounted = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        return choose(internal, external, internalFree, mounted)
    }
}
