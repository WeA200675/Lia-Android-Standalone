package de.wea200675.lia.core

import android.content.ContentResolver
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

data class ModelImportResult(val ok: Boolean, val message: String, val spec: ModelSpec? = null)

class TabletModelImporter(
    private val resolver: ContentResolver,
    private val registry: ModelRegistry,
    private val manifest: ModelManifest
) {
    fun import(uri: Uri): ModelImportResult {
        val name = resolver.query(uri, arrayOf("_display_name", "_size"), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        } ?: (uri.lastPathSegment?.substringAfterLast('/') ?: "")
        val spec = listOf(manifest.primary, manifest.recovery).firstOrNull { it.fileName.equals(name, true) }
            ?: return ModelImportResult(false, "Nur ein freigegebenes GGUF-Modell aus dem Katalog darf importiert werden.")
        val expectedBytes = resolver.query(uri, arrayOf("_size"), null, null, null)?.use {
            if (it.moveToFirst() && !it.isNull(0)) it.getLong(0) else -1L
        } ?: -1L
        if (expectedBytes <= 0L) return ModelImportResult(false, "Die Dateigröße konnte nicht sicher ermittelt werden.")
        registry.storageRejectionReason(expectedBytes)?.let { return ModelImportResult(false, it) }
        val target = registry.modelFile(spec)
        val temp = File(target.parentFile, target.name + ".part")
        return try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(temp).use { output ->
                    val buffer = ByteArray(1024 * 1024)
                    var copied = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } >= 0) {
                        if (read == 0) continue
                        copied += read
                        if (copied > expectedBytes || copied > registry.storageReport().availableBytes) {
                            return ModelImportResult(false, "Import wegen Speichergrenze abgebrochen.")
                        }
                        output.write(buffer, 0, read)
                    }
                    output.fd.sync()
                }
            } ?: return ModelImportResult(false, "Die Datei konnte nicht geöffnet werden.")
            val digest = sha256(temp)
            if (!digest.equals(spec.sha256, true)) {
                temp.delete()
                return ModelImportResult(false, "Sicherheitsprüfung fehlgeschlagen: SHA-256 stimmt nicht mit dem Katalog überein.")
            }
            if (!temp.renameTo(target)) {
                temp.delete()
                return ModelImportResult(false, "Das geprüfte Modell konnte nicht atomar aktiviert werden.")
            }
            ModelImportResult(true, "Modell " + spec.id + " importiert und verifiziert.", spec)
        } catch (e: Exception) {
            temp.delete()
            ModelImportResult(false, "Import fehlgeschlagen: " + (e.message ?: "unbekannter Fehler"))
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(1024 * 1024)
            var n: Int
            while (input.read(buffer).also { n = it } > 0) digest.update(buffer, 0, n)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
