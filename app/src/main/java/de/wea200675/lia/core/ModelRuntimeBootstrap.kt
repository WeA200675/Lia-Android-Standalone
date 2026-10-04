package de.wea200675.lia.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Creates an empty safe runtime immediately; model verification/loading is explicitly asynchronous. */
class ModelRuntimeBootstrap(
    private val context: Context,
    private val modelDirectory: File = ModelStorageLocator.forContext(context).directory
) {
    fun create(): LocalModelRuntime = LocalModelRuntime()

    fun createSupervised(): LocalModelRuntime = create()

    suspend fun loadInstalled(runtime: LocalModelRuntime): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            if (!ModelRuntimeAdmission.canStart(ModelStorageReporter.forDirectory(ModelStorageLocator.forContext(context)))) {
                return@runCatching false
            }
            val entry = ModelCatalog.entries.first()
            val model = ModelInstaller(context, modelDirectory).installedFile(entry) ?: return@runCatching false
            runtime.load(entry.spec(), model)
        }.getOrDefault(false)
    }
}
