package com.tabnine.general

import com.intellij.openapi.application.PathManager
import java.io.File
import java.util.zip.ZipFile

private const val TABNINE_ZIP_FILE = "TabNine.zip"

fun readTempTabninePluginZip(): List<String>? {
    val file = File(PathManager.getPluginTempPath(), TABNINE_ZIP_FILE)
    if (!file.exists()) return null

    return try {
        ZipFile(file).use { zip ->
            zip.entries().toList().map { it.name }
        }
    } catch (e: Throwable) {
        null
    }
}
