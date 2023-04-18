package com.tabnine.lifecycle

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.PluginNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.updateSettings.impl.PluginDownloader
import com.intellij.openapi.util.JDOMUtil
import com.intellij.util.text.SemVer
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.tabnine.dialogs.Dialogs
import com.tabnine.general.StaticConfig
import com.tabnine.general.StaticConfig.TABNINE_ENTERPRISE_ID_RAW
import com.tabnine.general.Utils
import java.net.URL
import java.util.concurrent.locks.ReentrantLock

data class TabninePluginDescriptor(
    @Tag("name") val name: String? = null,
    @Attribute("id") var id: String? = null,
    @Attribute("version") private var version: String? = null,
    @Attribute("url") var url: String? = null
) {
    val parsedVersion: SemVer?
        get() = SemVer.parseFromText(version)
}

class TabnineEnterprisePluginInstaller {
    private val downloadLock = ReentrantLock()
    fun installTabnineEnterprisePlugin() {
        val host = StaticConfig.getTabnineEnterpriseHost()
        if (!host.isPresent) return
        val plugin = getTabninePluginDescriptor(host.get()) ?: return

        ProgressManager.getInstance().run(object : Task.Modal(null, "Downloading Tabnine Enterprise Plugin", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    Utils.criticalSection(downloadLock) {
                        downloadAndInstall(indicator, plugin)
                    }
                } catch (e: Throwable) {
                    Logger.getInstance(javaClass).warn("Failed to install Tabnine Enterprise plugin", e)
                }
            }
        })
    }

    private fun getTabninePluginDescriptor(host: String): TabninePluginDescriptor? {
        val url = Utils.getTabnineCustomRepository(host) ?: return null
        val element = JDOMUtil.load(URL(url.get()))
        return XmlSerializer.deserialize(element.getChild("plugin"), TabninePluginDescriptor::class.java)
    }

    private fun downloadAndInstall(
        indicator: ProgressIndicator,
        plugin: TabninePluginDescriptor,
    ) {
        val newVersion = plugin.parsedVersion
        if (newVersion == null) {
            Logger.getInstance(javaClass).warn("Now downloading new version because was unable to find one. This shouldn't happen!")
            return
        }
        val downloadUrl = plugin.url
        if (downloadUrl == null) {
            Logger.getInstance(javaClass).warn("Now downloading new version because no url was supplied. This shouldn't happen!")
            return
        }

        val existingVersion =
            PluginManagerCore.getPlugins().firstOrNull { it.pluginId.idString == TABNINE_ENTERPRISE_ID_RAW }?.let {
                SemVer.parseFromText(it.version)
            }

        if (existingVersion?.let { it >= newVersion } == true) {
            Logger.getInstance(javaClass)
                .info("$TABNINE_ENTERPRISE_ID_RAW is already installed with version ${existingVersion.rawVersion}, which is >= the requested version $newVersion - skipping installation.")
            return
        }

        val downloader = createPluginDownloader(downloadUrl)

        if (!downloader.prepareToInstall(indicator)) {
            // the reason should appear in the logs because `prepareToInstall` have logged it - it's not available here.
            Logger.getInstance(javaClass).warn("Failed to prepare installation for $TABNINE_ENTERPRISE_ID_RAW")
            return
        }

        downloader.install()
        Dialogs.showRestartDialog("Tabnine enterprise plugin updated successfully - Restart is required")
    }

    private fun createPluginDownloader(downloadUrl: String): PluginDownloader {
        val ourPluginId = PluginId.getId(TABNINE_ENTERPRISE_ID_RAW)
        val pluginNode = PluginNode(ourPluginId)
        pluginNode.downloadUrl = downloadUrl
        return PluginDownloader.createDownloader(
            pluginNode,
            downloadUrl,
            null,
        )
    }
}
