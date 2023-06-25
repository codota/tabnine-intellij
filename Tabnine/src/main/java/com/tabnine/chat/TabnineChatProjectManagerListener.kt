package com.tabnine.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.lifecycle.BinaryCapabilitiesChangeNotifier
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JPanel

class TabnineChatProjectManagerListener private constructor() : ProjectManagerListener, Disposable {
    private val initialized = AtomicBoolean(false)
    private var messageRouter = ChatMessagesRouter()
    private val toolWindowIcon = IconLoader.findIcon("/icons/tabnine-tool-window-icon.svg")

    companion object {
        @Volatile
        private var instance: TabnineChatProjectManagerListener? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: TabnineChatProjectManagerListener().also { instance = it }
            }
    }

    fun start() {
        if (initialized.getAndSet(true)) return

        val connection = ApplicationManager.getApplication()
            .messageBus
            .connect(this)
        connection.subscribe(
            BinaryCapabilitiesChangeNotifier.CAPABILITIES_CHANGE_NOTIFIER_TOPIC,
            BinaryCapabilitiesChangeNotifier {
                connection.disconnect()

                // Since `projectOpened` is not called on IDE startup, we call it manually for all the open projects
                // before adding the listener.
                ProjectManager.getInstance().openProjects.forEach { projectOpened(it) }

                Logger.getInstance(javaClass).info("Starting Tabnine Chat project manager listener")

                ApplicationManager.getApplication().messageBus
                    .connect(this).subscribe(ProjectManager.TOPIC, this)
            }
        )
    }

    override fun projectOpened(project: Project) {
        Logger.getInstance(javaClass).info("Dispatching a tool window creation task for project ${project.name}")

        StartupManager.getInstance(project).runWhenProjectIsInitialized {
            registerChatToolWindow(project)
        }
    }

    private fun registerChatToolWindow(project: Project) {
        val alphaEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)
        val chatCapabilityEnabled = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.TABNINE_CHAT)
        val chatEnabled = chatCapabilityEnabled || alphaEnabled
        Logger.getInstance(javaClass)
            .info("Chat enabled: $chatEnabled (alpha: $alphaEnabled, chat capability: $chatCapabilityEnabled)")
        if (!chatEnabled) return

        ToolWindowManager.getInstance(project).registerToolWindow(
            RegisterToolWindowTask(
                id = "TabnineChat",
                anchor = ToolWindowAnchor.RIGHT,
                canCloseContent = false,
                component = JPanel(),
                icon = toolWindowIcon,
                contentFactory = TabnineChatWebViewFactory(messageRouter)
            )
        )
    }

    override fun dispose() {}
}
