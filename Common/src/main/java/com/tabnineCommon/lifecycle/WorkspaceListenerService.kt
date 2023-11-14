package com.tabnineCommon.lifecycle

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.tabnineCommon.binary.requests.fileLifecycle.Workspace
import com.tabnineCommon.general.DependencyContainer
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@Service
class WorkspaceListenerService {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()
    private val scheduler = AppExecutorUtil.getAppScheduledExecutorService()
    private val started = AtomicBoolean(false)

    fun start() {
        if (started.getAndSet(true)) return

        scheduler.scheduleWithFixedDelay(
            {
                val rootPaths = getWorkspaceRootPaths() ?: return@scheduleWithFixedDelay
                binaryRequestFacade.executeRequest(Workspace(rootPaths))
            },
            5, 30, TimeUnit.SECONDS
        )
    }

    fun getWorkspaceRootPaths(): List<String>? {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return null
        if (project.isDisposed) {
            Logger.getInstance(javaClass).warn("Project ${project.name} is disposed, skipping workspace update")
            return null
        }

        val rootPaths = mutableListOf<String>()
        for (contentRootUrl in ProjectRootManager.getInstance(project).contentRootUrls) {
            val url = URL(contentRootUrl)
            if (url.protocol != "file") {
                Logger.getInstance(javaClass).debug("Skipping workspace update for project ${project.name}, unsupported protocol ${url.protocol}")
                continue
            }
            rootPaths.add(url.path)
        }

        Logger.getInstance(javaClass).debug("Root paths for project ${project.name} found: $rootPaths")
        return rootPaths
    }
}
