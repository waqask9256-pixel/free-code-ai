package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ProjectFileEntity
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExporter {

    fun exportProjectToZip(
        context: Context,
        projectName: String,
        files: List<ProjectFileEntity>
    ): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }
            // Sanitize file name
            val safeName = projectName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val zipFile = File(exportDir, "${safeName}.zip")
            if (zipFile.exists()) {
                zipFile.delete()
            }

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (file in files) {
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    val bytes = file.content.toByteArray(StandardCharsets.UTF_8)
                    zos.write(bytes, 0, bytes.size)
                    zos.closeEntry()
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareZipFile(context: Context, uri: Uri, projectName: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$projectName (Free Code AI Project)")
            putExtra(Intent.EXTRA_TEXT, "Project files for $projectName exported from Free Code AI.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Download / Share $projectName.zip")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun shareSingleFile(context: Context, fileName: String, content: String) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }
            val file = File(exportDir, fileName)
            file.writeText(content)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Share $fileName")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
