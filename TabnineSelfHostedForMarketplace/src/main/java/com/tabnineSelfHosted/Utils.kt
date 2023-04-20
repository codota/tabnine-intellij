package com.tabnineSelfHosted

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.locks.ReentrantLock

object Utils {
    @JvmStatic
    fun setCustomRepository(url: String) {
        if (url.trim('/').isNotBlank()) {
            val pluginHosts = UpdateSettings.getInstance().storedPluginHosts
            val newStore = "${url.trim('/')}/update/jetbrains/updatePlugins.xml"
            pluginHosts.add(newStore)
            ContainerUtil.removeDuplicates(pluginHosts)
            logger<Utils>().debug(String.format("Added custom repository to %s", newStore))
        }
    }

    @JvmStatic
    fun replaceCustomRepository(oldUrl: String, newUrl: String) {
        val pluginHosts = UpdateSettings.getInstance().storedPluginHosts
        if (newUrl.trim('/').isNotBlank()) {
            val newStore = "${newUrl.trim('/')}/update/jetbrains/updatePlugins.xml"
            pluginHosts.add(newStore)
            Logger.getInstance(Utils::class.java)
                .debug(String.format("Added custom repository to %s", newStore))
        }
        if (oldUrl.trim('/').isNotBlank()) {
            val oldPluginRepo = "${oldUrl.trim('/')}/update/jetbrains/updatePlugins.xml"
            pluginHosts.remove(oldPluginRepo)
            Logger.getInstance(Utils::class.java)
                .debug(String.format("Removed custom repository from %s", oldPluginRepo))
        }
        ContainerUtil.removeDuplicates(pluginHosts)
    }

    @JvmStatic
    fun getTabnineCustomRepository(host: String?): String? {
        val sources = UpdateSettings.getInstance().storedPluginHosts
        return if (sources.isEmpty()) {
            null
        } else sources.firstOrNull { s: String -> s.contains(host!!) }
    }

    @JvmStatic
    fun <T> criticalSection(lock: ReentrantLock, block: Function0<T>): T {
        return try {
            lock.lock()
            val returnVal = block.invoke()
            lock.unlock()
            returnVal
        } catch (e: Throwable) {
            lock.unlock()
            throw e
        }
    }
}
