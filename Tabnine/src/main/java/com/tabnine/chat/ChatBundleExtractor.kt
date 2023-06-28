package com.tabnine.chat

import com.intellij.openapi.diagnostic.Logger
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

private fun untar(tarFile: InputStream, destinationDir: File) {
    destinationDir.mkdirs()
    TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(tarFile))).use { tais ->
        var entry: TarArchiveEntry? = tais.nextTarEntry
        while (entry != null) {
            val outputFile = File(destinationDir, entry.name)
            if (!outputFile.toPath().normalize().startsWith(destinationDir.toPath())) {
                Logger.getInstance("ChatBundleExtractor").warn("Bad tar entry: Entry '${outputFile.toPath()}' is outside of the target directory")
                continue
            }
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
