package com.tabnine.general

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.tabnine.UninstallListener
import java.io.File
import java.util.zip.ZipFile

private const val TABNINE_ZIP_FILE = "TabNine.zip"

data class TabnineZipFile(val contentFilenames: List<String>, val creationTimeMillis: Long)

fun readTempTabninePluginZip(): TabnineZipFile? {
    try {
        val pluginTempPath = PathManager.getPluginTempPath()
        Logger.getInstance(UninstallListener::class.java).info("Looking for $TABNINE_ZIP_FILE in: $pluginTempPath")
        val file = File(pluginTempPath, TABNINE_ZIP_FILE)
        if (!file.exists()) {
            Logger.getInstance(UninstallListener::class.java).warn("Could not find $TABNINE_ZIP_FILE in $pluginTempPath")
            return null
        }

        val contentFilenames = ZipFile(file).use { zip ->
            zip.entries().toList().map { it.name }
        }
        return TabnineZipFile(contentFilenames, file.lastModified())
    } catch (e: Throwable) {
        return null
    }
}
