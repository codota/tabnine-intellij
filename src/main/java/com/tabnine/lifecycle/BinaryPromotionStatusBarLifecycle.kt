package com.tabnine.lifecycle

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.statusBar.StatusBarPromotionBinaryRequest
import com.tabnine.binary.requests.statusBar.StatusBarPromotionShownRequest
import com.tabnine.general.StaticConfig.BINARY_PROMOTION_POLLING_DELAY
import com.tabnine.general.StaticConfig.BINARY_PROMOTION_POLLING_INTERVAL
import com.tabnine.statusBar.StatusBarPromotionWidget
import java.util.*

class BinaryPromotionStatusBarLifecycle(private val binaryRequestFacade: BinaryRequestFacade) {
    fun poll() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val statusBarPromotionWidget = getPromotionWidget()

                val promotion = binaryRequestFacade.executeRequest(StatusBarPromotionBinaryRequest())

                if(promotion != null) {
                    statusBarPromotionWidget?.isVisible = true;
                    statusBarPromotionWidget?.text = promotion.message
                    statusBarPromotionWidget?.id = promotion.id
                    statusBarPromotionWidget?.actions = promotion.actions
                    statusBarPromotionWidget?.notificationType = promotion.notificationType;

                    binaryRequestFacade.executeRequest(StatusBarPromotionShownRequest(promotion.message ?: "undefined"))
                } else {
                    clear(statusBarPromotionWidget)
                }
            }
        }, BINARY_PROMOTION_POLLING_DELAY, BINARY_PROMOTION_POLLING_INTERVAL)
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
        statusBarPromotionWidget?.text = null
        statusBarPromotionWidget?.id = null
        statusBarPromotionWidget?.isVisible = false
        statusBarPromotionWidget?.actions = null
        statusBarPromotionWidget?.notificationType = null
    }
}