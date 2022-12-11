package com.tabnine.inline

import com.intellij.openapi.application.ApplicationManager
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.analytics.EventRequest

class CompletionPreviewToggleEventSender(private val binaryRequestFacade: BinaryRequestFacade) {
    fun sendToggleEvent(order: CompletionOrder, index: Int) {
        ApplicationManager.getApplication().invokeLater {
            val eventOrder = if (order == CompletionOrder.NEXT) {
                "next"
            } else {
                "previous"
            }
            val eventName = "toggle-$eventOrder-suggestion"
            binaryRequestFacade.executeRequest(EventRequest(eventName, mapOf("suggestion_index" to index.toString())))
        }
    }
}
