package com.tabnine.statusBar

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.statusBar.StatusBarPromotionBinaryRequest
import com.tabnine.binary.requests.statusBar.StatusBarPromotionBinaryResponse
import com.tabnine.binary.requests.statusBar.StatusBarPromotionShownRequest
import java.util.concurrent.atomic.AtomicBoolean

class StatusBarUpdater(private val binaryRequestFacade: BinaryRequestFacade) {

    private companion object {
        val synchronizer = AtomicBoolean()
    }

    fun updateStatusBar() {
        ApplicationManager.getApplication().executeOnPooledThread { requestStatusBarMessage() }
    }

    fun requestStatusBarMessage() {
        try {
            if (!synchronizer.compareAndSet(false, true)) {
                return // abort if there's already a request in progress
            }
            val statusBarPromotionWidgets = getStatusBarsWidgets()
            if (statusBarPromotionWidgets.isEmpty()) {
                return
            }

            val promotion = binaryRequestFacade.executeRequest(StatusBarPromotionBinaryRequest())

            if (promotion != null) {
                updateStatusBars(statusBarPromotionWidgets, promotion)
                binaryRequestFacade.executeRequest(
                    StatusBarPromotionShownRequest(
                        promotion.id,
                        promotion.message ?: "undefined", promotion.notificationType,
                        promotion.state
                    )
                )
            } else {
                clear(statusBarPromotionWidgets)
            }
        } finally {
            synchronizer.set(false)
        }
    }

    private fun updateStatusBars(
        statusBarPromotionWidgets: List<StatusBarPromotionWidget.StatusBarPromotionComponent>,
        statusBarPromotionResponse: StatusBarPromotionBinaryResponse
    ) {
        updateStatusBars(
            statusBarPromotionWidgets, true,
            statusBarPromotionResponse.message,
            statusBarPromotionResponse.id, statusBarPromotionResponse.actions,
            statusBarPromotionResponse.notificationType
        )
    }

    private fun clear(statusBarPromotionWidgets: List<StatusBarPromotionWidget.StatusBarPromotionComponent>) {
        updateStatusBars(
            statusBarPromotionWidgets, false, null,
            null, null, null
        )
    }

    private fun updateStatusBars(
        statusBars: List<StatusBarPromotionWidget.StatusBarPromotionComponent>,
        isVisible: Boolean,
        text: String?,
        id: String?,
        actions: List<String>?,
        notificationType: String?,
    ) {
        for (statusBarPromotionWidget in statusBars) {
            statusBarPromotionWidget.isVisible = isVisible
            statusBarPromotionWidget.text = text
            statusBarPromotionWidget.id = id
            statusBarPromotionWidget.actions = actions
            statusBarPromotionWidget.notificationType = notificationType
        }
    }

    private fun getStatusBarsWidgets(): List<StatusBarPromotionWidget.StatusBarPromotionComponent> {
        return ProjectManager.getInstance().openProjects.mapNotNull {
            val statusBar = WindowManager.getInstance()?.getStatusBar(it)
            val widget = statusBar?.getWidget(StatusBarPromotionWidget::class.java.name) ?: null
            (widget as StatusBarPromotionWidget).component as StatusBarPromotionWidget.StatusBarPromotionComponent
        }
    }
}
