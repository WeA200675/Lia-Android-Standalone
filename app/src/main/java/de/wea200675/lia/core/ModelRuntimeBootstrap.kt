package de.wea200675.lia.core

import android.content.Context
import java.io.File

/** Safe app-start boundary for a verified user-installed model and offline fallback. */
class ModelRuntimeBootstrap(
    private val context: Context,
    private val modelDirectory: File = ModelStorageLocator.forContext(context).directory
) {
    fun create(): ModelRuntime {
        if (!ModelRuntimeAdmission.canStart(ModelStorageReporter.forDirectory(ModelStorageLocator.forContext(context)))) {
            return SafeOfflineRuntime()
        }
        return runCatching {
            LocalModelRuntimeFactory(modelDirectory).create(context)
        }.getOrElse { SafeOfflineRuntime() }
    }

    fun createSupervised(): ModelRuntime = create()
}
