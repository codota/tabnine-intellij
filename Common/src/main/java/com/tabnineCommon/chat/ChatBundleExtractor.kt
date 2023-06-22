package com.tabnineCommon.chat

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.jvm.Throws

private const val CHAT_BUNDLE_TAR_FILE = "chat-bundle.tar.gz"

object ChatBundleExtractor {

    @Throws(IllegalStateException::class)
    fun extractBundle(destination: Path) {
        val resource = javaClass.classLoader.getResourceAsStream(CHAT_BUNDLE_TAR_FILE)
            ?: throw IllegalStateException("Unable to find resource $CHAT_BUNDLE_TAR_FILE")
        untar(resource, destination.toFile())
    }
}

private fun untar(tarFile: InputStream, destDir: File) {
    destDir.mkdirs()
    TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(tarFile))).use { tais ->
        var entry: TarArchiveEntry? = tais.nextTarEntry
        while (entry != null) {
            if (entry.name == "./") {
                entry = tais.nextTarEntry
                continue
            }
            val outputFile = File(destDir, entry.name)
            if (entry.isDirectory) {
                outputFile.mkdirs()
            } else {
                val parent = outputFile.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }

                outputFile.writeBytes(tais.readAllBytes())
            }
            entry = tais.nextTarEntry
        }
    }
}
