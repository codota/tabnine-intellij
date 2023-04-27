package com.tabnineCommon

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.openapi.diagnostic.Logger
import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.uninstall.UninstallRequest
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.lifecycle.UninstallReporter
import kotlin.collections.any
import kotlin.jvm.javaClass
import kotlin.let

// An ugly workaround to distinguish between update and uninstall -->
// The uninstall flow contains this method in the call stack.
private const val uninstallMethodName = "uninstallAndUpdateUi"

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
        if (!Thread.currentThread().stackTrace.any { it.methodName == uninstallMethodName }
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
