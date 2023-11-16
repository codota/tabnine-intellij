package com.tabnineCommon.lifecycle

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.tabnineCommon.binary.requests.fileLifecycle.Workspace
import com.tabnineCommon.general.DependencyContainer
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

const val UPDATE_WORKSPACE_INITIAL_DELAY = 5L
const val UPDATE_WORKSPACE_INTERVAL = 30L

@Service
class WorkspaceListenerService {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()
    private val scheduler = AppExecutorUtil.getAppScheduledExecutorService()
    private val started = AtomicBoolean(false)

    fun start() {
        if (started.getAndSet(true)) return

        scheduler.scheduleWithFixedDelay(
            { updateWorkspaceRootPaths() },
            UPDATE_WORKSPACE_INITIAL_DELAY,
            UPDATE_WORKSPACE_INTERVAL,
            TimeUnit.SECONDS
        )
    }

    private fun updateWorkspaceRootPaths() {
        val rootPaths = ProjectManager.getInstance()
            .openProjects
            .map { getWorkspaceRootPaths(it) ?: emptyList() }
            .reduceOrNull { acc, cur -> acc.plus(cur) }

        if (rootPaths.isNullOrEmpty()) return
        Logger.getInstance(javaClass).info("All Root paths collected: $rootPaths")

        binaryRequestFacade.executeRequest(Workspace(rootPaths))
    }

    fun getWorkspaceRootPaths(project: Project): List<String>? {
        if (project.isDisposed) {
            Logger.getInstance(javaClass).warn("Project ${project.name} is disposed, skipping root paths resolution")
            return null
        }

        val rootPaths = mutableListOf<String>()
        for (contentRootUrl in ProjectRootManager.getInstance(project).contentRootUrls) {
            val url = URL(contentRootUrl)
            if (url.protocol != "file") {
                Logger.getInstance(javaClass).debug("$url in project ${project.name} has unsupported protocol (${url.protocol})")
                continue
            }
            rootPaths.add(url.path)
        }

        Logger.getInstance(javaClass).debug("Root paths for project ${project.name} found: $rootPaths")
        return rootPaths
    }
}