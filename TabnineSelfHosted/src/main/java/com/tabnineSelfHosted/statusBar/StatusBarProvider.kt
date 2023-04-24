package com.tabnineSelfHosted.statusBar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetProvider

class StatusBarProvider : StatusBarWidgetProvider {
    override fun getWidget(project: Project): StatusBarWidget {
        return TabnineSelfHostedStatusBarWidget(project)
    }

    override fun getAnchor(): String {
        return StatusBar.Anchors.before(StatusBar.StandardWidgets.POSITION_PANEL)
    }
}
