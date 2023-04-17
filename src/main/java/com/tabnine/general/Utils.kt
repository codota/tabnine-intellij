package com.tabnine.general

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.openapi.util.TextRange
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.containers.ContainerUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.Date
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

object Utils {
    private const val UNKNOWN = "Unknown"
    @JvmStatic
    val tabNinePluginVersion: String
        get() = tabNinePluginDescriptor.map(Function { obj: IdeaPluginDescriptor -> obj.version }).orElse(UNKNOWN)
    val tabNinePluginDescriptor: Optional<IdeaPluginDescriptor>
        get() = Arrays.stream(PluginManager.getPlugins())
            .filter(Predicate { plugin: IdeaPluginDescriptor -> StaticConfig.TABNINE_PLUGIN_ID == plugin.pluginId })
            .findAny()

    @JvmStatic
    fun endsWithADot(doc: Document, positionBeforeSuggestionPrefix: Int): Boolean {
        val begin = positionBeforeSuggestionPrefix - ".".length
        return if (begin < 0 || positionBeforeSuggestionPrefix > doc.textLength) {
            false
        } else {
            val tail = doc.getText(TextRange(begin, positionBeforeSuggestionPrefix))
            tail == "."
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readContent(inputStream: InputStream): String {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }
        return result.toString(StandardCharsets.UTF_8.name()).trim { it <= ' ' }
    }

    @JvmStatic
    fun toInt(aLong: Long?): Int {
        return if (aLong == null) {
            0
        } else Math.toIntExact(aLong)
    }

    @JvmStatic
    fun asLines(block: String): List<String> {
        return Arrays.stream(block.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .collect(Collectors.toList())
    }

    @JvmStatic
    fun cmdSanitize(text: String): String {
        return text.replace(" ", "")
    }

    @JvmStatic
    fun wrapWithHtml(content: String): String {
        return wrapWithHtmlTag(content, "html")
    }

    @JvmStatic
    fun wrapWithHtmlTag(content: String, tag: String): String {
        return "<$tag>$content</$tag>"
    }

    @JvmStatic
    fun getDaysDiff(date1: Date?, date2: Date?): Long {
        return if (date1 != null && date2 != null) {
            TimeUnit.DAYS.convert(
                Math.abs(date2.time - date1.time), TimeUnit.MILLISECONDS
            )
        } else -1
    }

    @JvmStatic
    fun getHoursDiff(date1: Date?, date2: Date?): Long {
        return if (date1 != null && date2 != null) {
            TimeUnit.HOURS.convert(date2.time - date1.time, TimeUnit.MILLISECONDS)
        } else -1
    }

    @JvmStatic
    fun executeUIThreadWithDelay(
        runnable: Runnable?,
        delay: Long,
        timeUnit: TimeUnit
    ): Future<*> {
        return executeThread(
            Runnable { ApplicationManager.getApplication().invokeLater(runnable!!) }, delay, timeUnit
        )
    }

    @JvmStatic
    fun executeThread(runnable: Runnable): Future<*> {
        if (isUnitTestMode) {
            runnable.run()
            return CompletableFuture.completedFuture<Any?>(null)
        }
        return AppExecutorUtil.getAppExecutorService().submit(runnable)
    }

    @JvmStatic
    fun executeThread(runnable: Runnable, delay: Long, timeUnit: TimeUnit): Future<*> {
        if (isUnitTestMode) {
            runnable.run()
            return CompletableFuture.completedFuture<Any?>(null)
        }
        return AppExecutorUtil.getAppScheduledExecutorService().schedule(runnable, delay, timeUnit)
    }

    @JvmStatic
    val isUnitTestMode: Boolean
        get() = (
            ApplicationManager.getApplication() == null ||
                ApplicationManager.getApplication().isUnitTestMode
            )

    @JvmStatic
    fun trimEndSlashAndWhitespace(text: String?): String? {
        return text?.replace("/\\s*$".toRegex(), "")
    }

    @JvmStatic
    fun setCustomRepository(url: String) {
        if (url.trim().isNotEmpty()) {
            val pluginHosts = UpdateSettings.getInstance().storedPluginHosts
            val newStore = String.format("%s/update/jetbrains/updatePlugins.xml", trimEndSlashAndWhitespace(url))
            pluginHosts.add(newStore)
            Logger.getInstance(Utils::class.java).debug(String.format("Added custom repository to %s", newStore))
            ContainerUtil.removeDuplicates(pluginHosts)
        }
    }

    @JvmStatic
    fun replaceCustomRepository(oldUrl: String, newUrl: String) {
        val pluginHosts = UpdateSettings.getInstance().storedPluginHosts
        if (newUrl.trim().isNotEmpty()) {
            val newStore = String.format("%s/update/jetbrains/updatePlugins.xml", trimEndSlashAndWhitespace(newUrl))
            pluginHosts.add(newStore)
            Logger.getInstance(Utils::class.java).debug(String.format("Added custom repository to %s", newStore))
        }
        if (oldUrl.trim().isNotEmpty()) {
            val oldPluginRepo =
                String.format("%s/update/jetbrains/updatePlugins.xml", trimEndSlashAndWhitespace(oldUrl))
            pluginHosts.remove(oldPluginRepo)
            Logger.getInstance(Utils::class.java)
                .debug(String.format("Removed custom repository from %s", oldPluginRepo))
        }
        ContainerUtil.removeDuplicates(pluginHosts)
    }
}
