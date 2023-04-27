package com.tabnineCommon.statusBar

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.statusBar.StatusBarPromotionBinaryRequest
import com.tabnineCommon.binary.requests.statusBar.StatusBarPromotionBinaryResponse
import com.tabnineCommon.binary.requests.statusBar.StatusBarPromotionShownRequest
import java.util.Timer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.timerTask

class StatusBarUpdater(private val binaryRequestFacade: BinaryRequestFacade) {

    private companion object {
        val synchronizer = AtomicBoolean()
        const val NO_MESSAGE = "undefined"
        const val DEFAULT_DURATION_MILLIS = 120000L // 2 minutes
    }

    private val timer = Timer()

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
                scheduleClearTask(promotion.id, promotion.durationSeconds)
                binaryRequestFacade.executeRequest(
                    StatusBarPromotionShownRequest(
                        promotion.id,
                        promotion.message ?: NO_MESSAGE,
                        promotion.notificationType,
                        promotion.state
                    )
                )
            }
        } finally {
            synchronizer.set(false)
        }
    }

    private fun scheduleClearTask(messageId: String?, durationSeconds: Long?) {
        timer.schedule(
            timerTask {
                val statusBarPromotionWidgets = getStatusBarsWidgets()
                    // clear only if it's the same message instance
                    .filter { it.id == messageId }
                clear(statusBarPromotionWidgets)
            },
            durationSeconds?.times(1000) ?: DEFAULT_DURATION_MILLIS
        )
    }

    private fun updateStatusBars(
        statusBarPromotionWidgets: List<StatusBarPromotionWidget.StatusBarPromotionComponent>,
        statusBarPromotionResponse: StatusBarPromotionBinaryResponse
    ) {
        for (statusBarPromotionWidget in statusBarPromotionWidgets) {
            statusBarPromotionWidget.isVisible = true
            statusBarPromotionWidget.text = statusBarPromotionResponse.message
            statusBarPromotionWidget.id = statusBarPromotionResponse.id
            statusBarPromotionWidget.actions = statusBarPromotionResponse.actions
            statusBarPromotionWidget.notificationType = statusBarPromotionResponse.notificationType
        }
    }

    private fun clear(statusBarPromotionWidgets: List<StatusBarPromotionWidget.StatusBarPromotionComponent>) {
        for (statusBarPromotionWidget in statusBarPromotionWidgets) {
            statusBarPromotionWidget.clearMessage()
        }
    }

    private fun getStatusBarsWidgets(): List<StatusBarPromotionWidget.StatusBarPromotionComponent> {
        return ProjectManager.getInstance().openProjects.mapNotNull {
            val statusBar = WindowManager.getInstance()?.getStatusBar(it)
            val widget = statusBar?.getWidget(StatusBarPromotionWidget::class.java.name)
            widget?.let { (widget as StatusBarPromotionWidget).component as StatusBarPromotionWidget.StatusBarPromotionComponent }
        }
    }
}
