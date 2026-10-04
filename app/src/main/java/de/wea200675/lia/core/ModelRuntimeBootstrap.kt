package de.wea200675.lia.core

import android.content.Context
import java.io.File

/** Safe app-start boundary for a verified user-installed model and offline fallback. */
class ModelRuntimeBootstrap(
    private val context: Context,
    private val modelDirectory: File = ModelStorageLocator.forContext(context).directory
) {
    fun create(): LocalModelRuntime {
        if (!ModelRuntimeAdmission.canStart(ModelStorageReporter.forDirectory(ModelStorageLocator.forContext(context)))) {
            return LocalModelRuntime()
        }
        return runCatching {
            LocalModelRuntimeFactory(modelDirectory).create(context)
        }.getOrElse { LocalModelRuntime() }
    }

    fun createSupervised(): LocalModelRuntime = create()
}
