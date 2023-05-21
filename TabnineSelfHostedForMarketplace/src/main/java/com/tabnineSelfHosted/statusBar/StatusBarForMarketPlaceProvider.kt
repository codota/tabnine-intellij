package com.tabnineSelfHosted.statusBar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetProvider

class StatusBarForMarketPlaceProvider : StatusBarWidgetProvider {
    override fun getWidget(project: Project): StatusBarWidget {
        return TabnineSelfHostedForMarketPlaceStatusBarWidget(project)
    }

    override fun getAnchor(): String {
        return StatusBar.Anchors.before(StatusBar.StandardWidgets.POSITION_PANEL)
    }
}
