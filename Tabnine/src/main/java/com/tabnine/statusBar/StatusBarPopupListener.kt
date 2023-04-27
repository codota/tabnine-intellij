package com.tabnine.statusBar

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.statusBar.StatusBarInteractionRequest
import com.tabnineCommon.general.IProviderOfThings

class StatusBarPopupListener : JBPopupListener {
    private val binaryRequestFacade: BinaryRequestFacade = service<IProviderOfThings>().binaryRequestFacade
    override fun beforeShown(event: LightweightWindowEvent) {
        binaryRequestFacade.executeRequest(StatusBarInteractionRequest())
    }
}
