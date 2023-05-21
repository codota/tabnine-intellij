package com.tabnineSelfHosted.statusBar

import com.intellij.ide.DataManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.MultipleTextValuesPresentation
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import com.tabnineSelfHosted.general.StaticConfig
import com.tabnineSelfHosted.statusBar.SelfHostedForMarketplaceStatusBarActions.buildStatusBarActionsGroup
import com.tabnineSelfHosted.userSettings.AppSettingsState.Companion.instance
import java.awt.event.MouseEvent
import javax.swing.Icon

class TabnineSelfHostedForMarketPlaceStatusBarWidget(project: Project) : EditorBasedWidget(project), StatusBarWidget, MultipleTextValuesPresentation {
    override fun getIcon(): Icon? {
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
        val hasCloud2UrlConfigured = hasCloud2UrlConfigured()
        return if (hasCloud2UrlConfigured) "Click to set the server URL for Tabnine Enterprise." else "Tabnine Enterprise Server URL: ${instance.cloud2Url}"
    }

    override fun getClickConsumer(): Consumer<MouseEvent>? {
        return null
    }

    override fun getPopupStep(): ListPopup {
        return JBPopupFactory.getInstance()
            .createActionGroupPopup(
                null,
                buildStatusBarActionsGroup(
                    if (myStatusBar != null) myStatusBar.project else null,
                ),
                DataManager.getInstance()
                    .getDataContext(if (myStatusBar != null) myStatusBar.component else null),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )
    }

    override fun getSelectedValue(): String {
        return "\u0000"
    }

    private fun update() {
        if (myStatusBar == null) {
            Logger.getInstance(javaClass).warn("Failed to update the status bar")
            return
        }
        myStatusBar.updateWidget(ID())
    }
}
