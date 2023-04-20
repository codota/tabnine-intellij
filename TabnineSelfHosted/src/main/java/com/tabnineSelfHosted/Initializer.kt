package com.tabnineSelfHosted

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.tabnineCommon.lifecycle.BinaryStateService
import com.tabnineCommon.logging.initTabnineLogger
import com.tabnineCommon.notifications.ConnectionLostNotificationHandler
import java.util.concurrent.atomic.AtomicBoolean

class Initializer : PreloadingActivity(), StartupActivity {
    override fun preload(indicator: ProgressIndicator) {
        initialize()
    }

    override fun runActivity(project: Project) {
        initialize()
    }

    private fun initialize() {
        if (initialized.getAndSet(true) || ApplicationManager.getApplication().isUnitTestMode) {
            return
        }
        initTabnineLogger()
        connectionLostNotificationHandler.startConnectionLostListener()
        ServiceManager.getService(BinaryStateService::class.java).startUpdateLoop()
        SelfHostedInitializer().initialize()
    }

    companion object {
        private val connectionLostNotificationHandler = ConnectionLostNotificationHandler()
        private val initialized = AtomicBoolean(false)
    }
}
