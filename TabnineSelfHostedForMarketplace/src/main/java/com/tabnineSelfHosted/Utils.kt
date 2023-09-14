package com.tabnineSelfHosted

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.locks.ReentrantLock

object Utils {
    @JvmStatic
    fun setCustomRepository(url: String) {
        val trimmedUrl = trimEndSlashAndWhitespace(url)
        if (trimmedUrl.isNotBlank()) {
            val pluginHosts = UpdateSettings.getInstance().storedPluginHosts
            val newStore = "$trimmedUrl/update/jetbrains/updatePlugins.xml"
            pluginHosts.add(newStore)
            ContainerUtil.removeDuplicates(pluginHosts)
            logger<Utils>().debug("Added custom repository to $newStore")
        }
    }
    @JvmStatic
    fun replaceCustomRepository(oldUrl: String, newUrl: String) {
        val trimmedNewUrl = trimEndSlashAndWhitespace(newUrl)
        val trimmedOldUrl = trimEndSlashAndWhitespace(oldUrl)
        if (trimmedNewUrl.equals(trimmedOldUrl, true)) {
            Logger.getInstance(javaClass).info("No need to replace anything, $trimmedNewUrl == $trimmedOldUrl")
            return
        }

        val pluginHosts = UpdateSettings.getInstance().storedPluginHosts
        if (trimmedNewUrl.isNotBlank()) {
            val newStore = "$trimmedNewUrl/update/jetbrains/updatePlugins.xml"
            pluginHosts.add(newStore)
            Logger.getInstance(Utils::class.java)
                .debug(String.format("Added custom repository to %s", newStore))
        }
        if (trimmedOldUrl.isNotBlank()) {
            val oldPluginRepo = "$trimmedOldUrl/update/jetbrains/updatePlugins.xml"
            pluginHosts.remove(oldPluginRepo)
            Logger.getInstance(Utils::class.java)
                .debug("Removed custom repository from $oldPluginRepo")
        }
        ContainerUtil.removeDuplicates(pluginHosts)
    }

    @JvmStatic
    fun getTabnineCustomRepository(host: String): String? {
        val trimmedHost = trimEndSlashAndWhitespace(host)
        val sources = UpdateSettings.getInstance().storedPluginHosts
        return if (sources.isEmpty()) {
            null
        } else sources.firstOrNull { s: String -> s.contains(trimmedHost) }
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

    @JvmStatic
    fun trimEndSlashAndWhitespace(text: String): String {
        return text.replace("/*\\s*$".toRegex(), "")
    }
}
