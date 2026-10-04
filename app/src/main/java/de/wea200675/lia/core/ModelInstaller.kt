package de.wea200675.lia.core

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

data class ModelImportResult(val file: File?, val sha256: String?, val error: String?) {
    val succeeded: Boolean get() = file != null && sha256 != null && error == null
}

/** Copies a user-selected model into app-private storage and verifies it before activating it. */
class ModelInstaller(private val context: Context, private val directory: File) {
    fun install(uri: Uri, entry: ModelCatalogEntry): ModelImportResult {
        val expectedSha256 = entry.sha256
        if (!expectedSha256.matches(Regex("[0-9a-fA-F]{64}"))) {
            return ModelImportResult(null, null, "Ein gültiger SHA-256-Wert ist erforderlich.")
        }
        if (!directory.exists() && !directory.mkdirs()) {
            return ModelImportResult(null, null, "Der private Modellspeicher ist nicht verfügbar.")
        }
        val safeName = File(entry.fileName).name
        if (safeName != entry.fileName || !safeName.endsWith(".gguf", ignoreCase = true)) {
            return ModelImportResult(null, null, "Ungültiger Modell-Dateiname.")
        }
        val temporary = File(directory, ".$safeName.importing")
        val target = File(directory, safeName)
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(temporary).use { fileOutput ->
                    val output = fileOutput.buffered()
                    val buffer = ByteArray(1024 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        digest.update(buffer, 0, count)
                        output.write(buffer, 0, count)
                    }
                    output.flush()
                    fileOutput.fd.sync()
            } ?: return ModelImportResult(null, null, "Die ausgewählte Datei ist nicht lesbar.")
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            if (!actual.equals(expectedSha256, ignoreCase = true)) {
                temporary.delete()
                return ModelImportResult(null, actual, "SHA-256 stimmt nicht mit dem bestätigten Modell-Hash überein.")
            }
            if (target.exists() && !target.delete()) {
                temporary.delete()
                return ModelImportResult(null, actual, "Das vorhandene Modell konnte nicht ersetzt werden.")
            }
            if (!temporary.renameTo(target)) {
                temporary.delete()
                return ModelImportResult(null, actual, "Das geprüfte Modell konnte nicht aktiviert werden.")
            }
            context.getSharedPreferences("lia_models", Context.MODE_PRIVATE).edit()
                .putString("active_model_id", entry.id)
                .putString("active_model_file", safeName)
                .putString("active_model_sha256", actual)
                .putString("active_model_license", entry.license)
                .apply()
            ModelImportResult(target, actual, null)
        } catch (error: Exception) {
            temporary.delete()
            ModelImportResult(null, null, error.message ?: "Modellimport fehlgeschlagen.")
        }
    }

    fun installedFile(entry: ModelCatalogEntry): File? {
        val preferences = context.getSharedPreferences("lia_models", Context.MODE_PRIVATE)
        if (preferences.getString("active_model_id", null) != entry.id) return null
        if (preferences.getString("active_model_file", null) != entry.fileName) return null
        val expected = preferences.getString("active_model_sha256", null) ?: return null
        if (!expected.equals(entry.sha256, ignoreCase = true)) return null
        val model = File(directory, entry.fileName)
        return model.takeIf { ModelVerifier.verified(it, entry.sha256) }
    }

    fun remove(entry: ModelCatalogEntry): Boolean {
        val preferences = context.getSharedPreferences("lia_models", Context.MODE_PRIVATE)
        if (preferences.getString("active_model_id", null) != entry.id) return false
        val deleted = File(directory, entry.fileName).delete()
        preferences.edit().clear().apply()
        return deleted
    }
}
