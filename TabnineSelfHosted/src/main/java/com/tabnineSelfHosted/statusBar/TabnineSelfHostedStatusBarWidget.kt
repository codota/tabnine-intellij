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
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.lifecycle.BinaryStateSingleton
import com.tabnineCommon.state.CompletionsState.isCompletionsEnabled
import com.tabnineCommon.state.CompletionsStateNotifier
import com.tabnineSelfHosted.binary.lifecycle.UserInfoStateSingleton
import com.tabnineSelfHosted.statusBar.SelfHostedStatusBarActions.buildStatusBarActionsGroup
import java.awt.event.MouseEvent
import javax.swing.Icon

class TabnineSelfHostedStatusBarWidget(project: Project) :
    EditorBasedWidget(project),
    StatusBarWidget,
    MultipleTextValuesPresentation {
    @Volatile
    private var userInfoResponse = UserInfoStateSingleton.instance.get()
    @Volatile
    private var connectionHealthStatus =
        BinaryStateSingleton.instance.get()?.cloudConnectionHealthStatus

    init {
        BinaryStateSingleton.instance.onChange(this) {
            connectionHealthStatus = it.cloudConnectionHealthStatus

            update()
        }

        UserInfoStateSingleton.instance.onChange(this) {
            userInfoResponse = it
            update()
        }

        CompletionsStateNotifier.subscribe(object : CompletionsStateNotifier {
            override fun stateChanged(isEnabled: Boolean) {
                update()
            }
        })
    }

    override fun getIcon(): Icon {
        val cloudConnectionHealthStatus = connectionHealthStatus
        val userInfo = userInfoResponse
        val hasCloud2UrlConfigured = hasCloud2UrlConfigured()
        if (!hasCloud2UrlConfigured || cloudConnectionHealthStatus != CloudConnectionHealthStatus.Ok || userInfo == null || !userInfo.isLoggedIn || userInfo.team == null) {
            return StaticConfig.getProblemGlyphIcon()
        }

        return StaticConfig.getGlyphIcon()
    }

    private fun hasCloud2UrlConfigured(): Boolean {
        return (
            StaticConfig.getTabnineEnterpriseHost().isPresent && StaticConfig.getTabnineEnterpriseHost()
                .get().isNotBlank()
            )
    }

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
        if (!hasCloud2UrlConfigured()) {
            return "Click to set the server URL."
        }

        val userInfo = userInfoResponse ?: return "Initializing..."

        if (userInfo.email.isBlank()) {
            return "Click for sign in to use Tabnine Enterprise."
        }
        val suffix = "Server URL: ${StaticConfig.getTabnineEnterpriseHost().get()}"

        return "Connected to Tabnine Enterprise as ${userInfo.email}. $suffix"
    }

    override fun getClickConsumer(): Consumer<MouseEvent>? {
        return null
    }

    override fun getPopupStep(): ListPopup {
        val cloudConnectionHealthStatus = connectionHealthStatus
        val userInfo = userInfoResponse
        return JBPopupFactory.getInstance().createActionGroupPopup(
            null,
            buildStatusBarActionsGroup(
                if (myStatusBar != null) myStatusBar.project else null,
                getUserLoginStatus(cloudConnectionHealthStatus, userInfo?.email)
            ),
            DataManager.getInstance()
                .getDataContext(if (myStatusBar != null) myStatusBar.component else null),
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            true
        )
    }

    override fun getSelectedValue(): String {
        if (!hasCloud2UrlConfigured()) {
            return "Tabnine Enterprise: Set your Tabnine URL"
        }
        val cloudConnectionHealthStatus =
            connectionHealthStatus ?: return "Tabnine Enterprise: Initializing"

        if (cloudConnectionHealthStatus != CloudConnectionHealthStatus.Ok) {
            return "Tabnine Enterprise: Server connectivity issue"
        }

        val userInfo = userInfoResponse ?: return "Tabnine Enterprise: Initializing"

        if (!userInfo.isLoggedIn) {
            return "Tabnine Enterprise: Sign in using your Tabnine account"
        }

        if (userInfo.team == null) {
            return "Tabnine Enterprise: Not part of a team"
        }

        if (!isCompletionsEnabled()) {
            return "Tabnine Enterprise: Disabled"
        }

        return "Tabnine Enterprise"
    }

    private fun update() {
        if (myStatusBar == null) {
            Logger.getInstance(javaClass).warn("Failed to update the status bar")
            return
        }
        myStatusBar.updateWidget(ID())
    }
}
