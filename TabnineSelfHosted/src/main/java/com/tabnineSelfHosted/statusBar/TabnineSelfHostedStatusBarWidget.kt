package com.tabnineSelfHosted.statusBar

import com.intellij.ide.DataManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.MultipleTextValuesPresentation
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.binary.requests.config.StateResponse
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier
import com.tabnineSelfHosted.statusBar.SelfHostedStatusBarActions.buildStatusBarActionsGroup
import java.awt.event.MouseEvent
import javax.swing.Icon

class TabnineSelfHostedStatusBarWidget(project: Project) : EditorBasedWidget(project), StatusBarWidget, MultipleTextValuesPresentation {
    private var cloudConnectionHealthStatus = CloudConnectionHealthStatus.Ok
    private var username: String? = null

    init {
        // register for state changes (we will get notified whenever the state changes)
        ApplicationManager.getApplication()
            .messageBus
            .connect(this)
            .subscribe(
                BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
                BinaryStateChangeNotifier { (_, _, _, cloudConnectionHealthStatus1, _, userName): StateResponse ->
                    cloudConnectionHealthStatus = cloudConnectionHealthStatus1
                    username = userName
                    update()
                }
            )
    }

    override fun getIcon(): Icon? {
        if (cloudConnectionHealthStatus === CloudConnectionHealthStatus.Failed) {
            return StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE
        }
        val hasCloud2UrlConfigured = hasCloud2UrlConfigured()

        return if (hasCloud2UrlConfigured && !username.isNullOrBlank()) {
            StaticConfig.ICON_AND_NAME_ENTERPRISE
        } else StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE
    }

    private fun hasCloud2UrlConfigured(): Boolean {
        return (
            StaticConfig.getTabnineEnterpriseHost().isPresent &&
                StaticConfig.getTabnineEnterpriseHost().get().isNotBlank()
            )
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
        if (username.isNullOrBlank()) {
            return "Click for sign in to use Tabnine Enterprise."
        }
        val hasCloud2UrlConfigured = hasCloud2UrlConfigured()
        val suffix = if (hasCloud2UrlConfigured) "Click to set the server URL." else "Server URL: ${StaticConfig.getTabnineEnterpriseHost().get()}"

        return "Connected to Tabnine Enterprise as $username. $suffix"
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
                    !username.isNullOrBlank()
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
