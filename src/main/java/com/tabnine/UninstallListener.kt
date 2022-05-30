package com.tabnine

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.openapi.diagnostic.Logger
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.uninstall.UninstallRequest
import com.tabnine.general.StaticConfig
import com.tabnine.lifecycle.UninstallReporter

// An ugly workaround to distinguish between update and uninstall -->
// If it's an update, this method name will appear in the stack trace.
private const val updateMethodName = "installOrUpdatePlugin"

class UninstallListener(
    private val facade: BinaryRequestFacade,
    private val uninstallReporter: UninstallReporter,
) :
    PluginStateListener {

    override fun install(descriptor: IdeaPluginDescriptor) {
        // nothing to do here
    }

    override fun uninstall(descriptor: IdeaPluginDescriptor) {
        if (descriptor.pluginId?.let { it == StaticConfig.TABNINE_PLUGIN_ID } == false) {
            return
        }
        val stackTrace = Thread.currentThread().stackTrace
        if (stackTrace.any { it.methodName == updateMethodName }
        ) {
            Logger.getInstance(javaClass).info("Tabnine plugin detected version update.")
            return
        }

        Logger.getInstance(javaClass).warn("Uninstalling Tabnine... :(")

        if (facade.executeRequest(UninstallRequest()) == null) {
            Logger.getInstance(javaClass)
                .warn("Failed to send uninstall request to Tabnine, performing fallback operation")
            uninstallReporter.reportUninstall(emptyMap())
        }
    }
}
