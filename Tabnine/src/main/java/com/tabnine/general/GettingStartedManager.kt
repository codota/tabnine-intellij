package com.tabnine.general

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.tabnineCommon.config.Config
import com.tabnineCommon.general.Utils.executeUIThreadWithDelay
import com.tabnineCommon.lifecycle.PluginInstalledNotifier
import java.util.concurrent.TimeUnit

const val PAGE_TITLE = "Tabnine - Getting Started"
const val PAGE_URL = "https://www.tabnine.com/getting-started/intellij?origin=ide"
const val IS_GETTING_STARTED_OPENED_KEY = "is-getting-started-opened"
const val AFTER_PROJECT_OPENED_DELAY_SECONDS = 15L

class GettingStartedManager : PreloadingActivity(), Disposable {
    private val browserUtilsService = BrowserUtilsService.instance

    companion object {
        @JvmStatic
        val instance = GettingStartedManager()
    }

    override fun preload(indicator: ProgressIndicator) {
        if (!Config.IS_SELF_HOSTED) {
            registerForPluginInstalled()
        }
    }

    private fun registerForPluginInstalled() {
        ApplicationManager.getApplication()
            .messageBus
            .connect(this)
            .subscribe(
                PluginInstalledNotifier.PLUGIN_INSTALLED_TOPIC,
                PluginInstalledNotifier {
                    handleFirstTimePreview()
                }
            )
    }

    private fun handleFirstTimePreview() {
        executeUIThreadWithDelay(
            {
                if (!isPageShown()) {
                    browserUtilsService.openPageOnFocusedProject(PAGE_TITLE, PAGE_URL)
                    markPageAsShown()
                }
            },
            AFTER_PROJECT_OPENED_DELAY_SECONDS,
            TimeUnit.SECONDS
        )
    }
    fun openPageOnProject(project: Project) {
        browserUtilsService.openUrlOnSplitWindow(project, PAGE_TITLE, PAGE_URL)
        markPageAsShown()
    }

    private fun isPageShown(): Boolean {
        return PropertiesComponent.getInstance().getBoolean(IS_GETTING_STARTED_OPENED_KEY)
    }

    private fun markPageAsShown() {
        PropertiesComponent.getInstance().setValue(IS_GETTING_STARTED_OPENED_KEY, true)
    }

    override fun dispose() {
    }
}
