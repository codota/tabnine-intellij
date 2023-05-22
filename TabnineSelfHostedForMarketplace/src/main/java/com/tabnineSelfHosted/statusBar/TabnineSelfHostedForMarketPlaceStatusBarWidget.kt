package com.tabnineSelfHosted.statusBar

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import com.tabnineSelfHosted.TabnineEnterprisePluginInstaller
import com.tabnineSelfHosted.dialogs.TabnineEnterpriseUrlDialogWrapper
import com.tabnineSelfHosted.general.StaticConfig
import com.tabnineSelfHosted.userSettings.AppSettingsState.Companion.instance
import java.awt.event.MouseEvent
import javax.swing.Icon

class TabnineSelfHostedForMarketPlaceStatusBarWidget(project: Project) : EditorBasedWidget(project), StatusBarWidget, StatusBarWidget.IconPresentation {
    override fun getIcon(): Icon {
        return if (hasCloud2UrlConfigured()) {
            StaticConfig.ICON_AND_NAME_ENTERPRISE
        } else StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE
    }

    private fun hasCloud2UrlConfigured(): Boolean {
        return instance.cloud2Url.isNotBlank()
    }

    // Compatability implementation. DO NOT ADD @Override.
    override fun getPresentation(): StatusBarWidget.WidgetPresentation {
        return this
    }

    // Compatability implementation. DO NOT ADD @Override.
    override fun getPresentation(type: StatusBarWidget.PlatformType): StatusBarWidget.WidgetPresentation {
        return this
    }

    override fun ID(): String {
        return javaClass.name
    }

    // Compatability implementation. DO NOT ADD @Override.
    override fun getTooltipText(): String {
        return if (!hasCloud2UrlConfigured()) "Click to set the server URL for Tabnine Enterprise." else "Tabnine Enterprise Server URL: ${instance.cloud2Url}."
    }

    override fun getClickConsumer(): Consumer<MouseEvent>? {
        return Consumer<MouseEvent> { mouseEvent ->
            if (mouseEvent.isPopupTrigger || MouseEvent.BUTTON1 != mouseEvent.button) {
                return@Consumer
            }
            Logger.getInstance(javaClass).info("Opening Tabnine enterprise cloud url dialog")
            val dialog = TabnineEnterpriseUrlDialogWrapper(instance.cloud2Url)
            if (dialog.showAndGet()) {
                val url = dialog.inputData
                instance.cloud2Url = url
                update()
                TabnineEnterprisePluginInstaller().installTabnineEnterprisePlugin(url)
            }
        }
    }

    private fun update() {
        if (myStatusBar == null) {
            Logger.getInstance(javaClass).warn("Failed to update the status bar")
            return
        }
        myStatusBar.updateWidget(ID())
    }
}
