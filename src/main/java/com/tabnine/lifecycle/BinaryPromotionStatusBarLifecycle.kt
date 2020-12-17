package com.tabnine.lifecycle

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.statusBar.StatusBarPromotionBinaryRequest
import com.tabnine.binary.requests.statusBar.StatusBarPromotionShownRequest
import com.tabnine.general.StaticConfig
import com.tabnine.statusBar.StatusBarPromotionWidget
import java.util.*

class BinaryPromotionStatusBarLifecycle(private val binaryRequestFacade: BinaryRequestFacade) {
    fun poll() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val statusBarPromotionWidget = getPromotionWidget()

                val promotion = binaryRequestFacade.executeRequest(StatusBarPromotionBinaryRequest())

                promotion?.let {
                    statusBarPromotionWidget?.isVisible = true;
                    statusBarPromotionWidget?.text = it.message
                    statusBarPromotionWidget?.id = it.id

                    binaryRequestFacade.executeRequest(StatusBarPromotionShownRequest(it.message ?: "undefined"))
                } ?: clear(statusBarPromotionWidget)
            }
        }, StaticConfig.BINARY_PROMOTION_POLLING_DELAY, StaticConfig.BINARY_PROMOTION_POLLING_INTERVAL)
    }

    private fun getPromotionWidget(): StatusBarPromotionWidget.StatusBarPromotionComponent? {
        val openProjects = ProjectManager.getInstance().openProjects

        if (openProjects.isEmpty()) {
            return null
        }

        val statusBar = WindowManager.getInstance().getStatusBar(openProjects[0])
        val promotionWidget = statusBar.getWidget(StatusBarPromotionWidget::class.java.name) as StatusBarPromotionWidget

        return promotionWidget.component as StatusBarPromotionWidget.StatusBarPromotionComponent
    }

    private fun clear(statusBarPromotionWidget: StatusBarPromotionWidget.StatusBarPromotionComponent?) {
        statusBarPromotionWidget?.isVisible = false;
        statusBarPromotionWidget?.text = null
        statusBarPromotionWidget?.id = null
    }
}