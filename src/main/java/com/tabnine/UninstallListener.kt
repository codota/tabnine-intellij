package com.tabnine

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.text.SemVer
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.uninstall.UninstallRequest
import com.tabnine.general.StaticConfig
import com.tabnine.general.readTempTabninePluginZip
import com.tabnine.lifecycle.UninstallReporter
import java.nio.file.Paths
import java.time.Duration

private const val TABNINE_JAR_NAME = "TabNine-"
private const val JAR_SUFFIX = ".jar"
private val TABNINE_JAR_REGEX = """^$TABNINE_JAR_NAME\d+\.\d+\.\d+(-\w+.\d+)*.jar""".toRegex()

class UninstallListener(
    private val facade: BinaryRequestFacade,
    private val uninstallReporter: UninstallReporter,
    staleFileDuration: Duration
) :
    PluginStateListener {
    private val staleFileDurationMillis: Long = staleFileDuration.toMillis()

    override fun install(descriptor: IdeaPluginDescriptor) {
        // nothing to do here
    }

    override fun uninstall(descriptor: IdeaPluginDescriptor) {
        if (descriptor.pluginId?.let { it == StaticConfig.TABNINE_PLUGIN_ID } == false) {
            return
        }
        if (newerVersionExists(descriptor)) {
            Logger.getInstance(javaClass)
                .info("Tabnine plugin detected version update.")
            return
        }

        Logger.getInstance(javaClass).warn("Uninstalling Tabnine... :(")

        if (facade.executeRequest(UninstallRequest()) == null) {
            Logger.getInstance(javaClass)
                .warn("Failed to send uninstall request to Tabnine, performing fallback operation")
            uninstallReporter.reportUninstall(emptyMap())
        }
    }

    private fun newerVersionExists(descriptor: IdeaPluginDescriptor): Boolean {
        val currentVersion = descriptor.version?.let { SemVer.parseFromText(it) } ?: return false
        val tempTabninePluginZipFile = readTempTabninePluginZip() ?: return false
        if (System.currentTimeMillis() - tempTabninePluginZipFile.creationTimeMillis > staleFileDurationMillis) return false

        return tempTabninePluginZipFile.contentFilenames.any {
            fileVersionIsNewerThan(it, currentVersion)
        }
    }

    private fun fileVersionIsNewerThan(filename: String, version: SemVer): Boolean =
        TABNINE_JAR_REGEX.find(Paths.get(filename).fileName.toString())
            ?.let { match ->
                val fileVersion =
                    match.value.substring(TABNINE_JAR_NAME.length, match.value.length - JAR_SUFFIX.length)
                SemVer.parseFromText(fileVersion)
            }?.let { semver ->
                semver > version
            } ?: false
}
