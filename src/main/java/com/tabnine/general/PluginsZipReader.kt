package com.tabnine.general

import com.intellij.openapi.application.PathManager
import java.io.File
import java.util.zip.ZipFile

private const val TABNINE_ZIP_FILE = "TabNine.zip"

data class TabnineZipFile(val contentFilenames: List<String>, val creationTimeMillis: Long)

fun readTempTabninePluginZip(): TabnineZipFile? {
    try {
        val file = File(PathManager.getPluginTempPath(), TABNINE_ZIP_FILE)
        if (!file.exists()) return null

        val contentFilenames = ZipFile(file).use { zip ->
            zip.entries().toList().map { it.name }
        }
        return TabnineZipFile(contentFilenames, file.lastModified())
    } catch (e: Throwable) {
        return null
    }
}
