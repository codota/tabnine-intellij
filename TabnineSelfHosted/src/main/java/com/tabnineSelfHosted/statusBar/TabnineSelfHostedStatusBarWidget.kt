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
import com.tabnineCommon.lifecycle.BinaryStateChangeNotifier
import com.tabnineSelfHosted.binary.lifecycle.UserInfoChangeNotifier
import com.tabnineSelfHosted.binary.requests.userInfo.UserInfoResponse
import com.tabnineSelfHosted.general.StaticConfig
import com.tabnineSelfHosted.statusBar.SelfHostedStatusBarActions.buildStatusBarActionsGroup
import com.tabnineSelfHosted.userSettings.AppSettingsState
import java.awt.event.MouseEvent
import javax.swing.Icon

class TabnineSelfHostedStatusBarWidget(project: Project) :
    EditorBasedWidget(project),
    StatusBarWidget,
    MultipleTextValuesPresentation {
    private var cloudConnectionHealthStatus = CloudConnectionHealthStatus.Ok
    private var userInfo: UserInfoResponse? = null

    init {
        ApplicationManager.getApplication()
            .messageBus
            .connect(this)
            .subscribe(
                BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
                BinaryStateChangeNotifier { (_, _, _, cloudConnectionHealthStatus, _, _): StateResponse ->
                    this.cloudConnectionHealthStatus = cloudConnectionHealthStatus
                    update()
                }
            )

        ApplicationManager.getApplication()
            .messageBus
            .connect(this)
            .subscribe(
                UserInfoChangeNotifier.USER_INFO_CHANGED_TOPIC,
                UserInfoChangeNotifier { userInfoResponse ->
                    userInfo = userInfoResponse
                    update()
                }
            )
    }

    override fun getIcon(): Icon {
        val hasCloud2UrlConfigured = hasCloud2UrlConfigured()
        if (!hasCloud2UrlConfigured) {
            return StaticConfig.ICON_AND_NAME_NO_URL
        }

        if (cloudConnectionHealthStatus === CloudConnectionHealthStatus.Failed) {
            return StaticConfig.ICON_AND_NAME_CONNECTIVITY_ISSUES
        }

        if (userInfo == null || userInfo?.isLoggedIn == false) {
            return StaticConfig.ICON_AND_NAME_NOT_LOGGED_IN_ENTERPRISE
        }

        if (userInfo?.team == null) {
            return StaticConfig.ICON_AND_NAME_NOT_IN_TEAM
        }

        return StaticConfig.ICON_AND_NAME_ENTERPRISE
    }

    private fun hasCloud2UrlConfigured(): Boolean {
        return AppSettingsState.instance.cloud2Url.isNotBlank()
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
        if ((userInfo?.email).isNullOrBlank()) {
            return "Click for sign in to use Tabnine Enterprise."
        }
        val hasCloud2UrlConfigured = hasCloud2UrlConfigured()
        val suffix = if (!hasCloud2UrlConfigured) "Click to set the server URL." else "Server URL: ${
        AppSettingsState.instance.cloud2Url
        }"

        return "Connected to Tabnine Enterprise as ${userInfo!!.email}. $suffix"
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
                    !(userInfo?.email).isNullOrBlank()
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
