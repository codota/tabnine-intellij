package com.tabnineSelfHosted

import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class Initializer : PreloadingActivity(), StartupActivity {
    override fun preload(indicator: ProgressIndicator) {
        SelfHostedInitializer().initialize()
    }

    override fun runActivity(project: Project) {
        SelfHostedInitializer().initialize()
    }
}
