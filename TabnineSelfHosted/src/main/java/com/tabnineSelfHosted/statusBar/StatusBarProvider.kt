package com.tabnineSelfHosted.statusBar

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class StatusBarProvider : StatusBarWidgetFactory {
    override fun getId(): String {
        return javaClass.name
    }

    override fun getDisplayName(): String {
        return "Tabnine"
    }

    override fun isAvailable(project: Project): Boolean {
        return true
    }

    override fun createWidget(project: Project): StatusBarWidget {
        Logger.getInstance(javaClass).info("creating (self hosted) status bar widget")
        return TabnineSelfHostedStatusBarWidget(project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Logger.getInstance(javaClass).info("disposing (self hosted) status bar widget")
        Disposer.dispose(widget)
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean {
        return true
    }
}
