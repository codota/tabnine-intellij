package com.tabnineCommon.general

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.tabnineCommon.binary.BinaryRequest
import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.BinaryResponse
import com.tabnineCommon.binary.requests.analytics.EventRequest
import com.tabnineCommon.binary.requests.notifications.shown.SuggestionDroppedReason
import com.tabnineCommon.binary.requests.notifications.shown.SuggestionDroppedRequest
import com.tabnineCommon.capabilities.RenderingMode
import com.tabnineCommon.inline.CompletionOrder
import com.tabnineCommon.prediction.CompletionFacade.getFilename
import com.tabnineCommon.prediction.TabNineCompletion

class CompletionsEventSender(private val binaryRequestFacade: BinaryRequestFacade) {
    fun sendToggleInlineSuggestionEvent(order: CompletionOrder, index: Int) {
        val eventOrder = if (order == CompletionOrder.NEXT) {
            "next"
        } else {
            "previous"
        }
        val eventName = "toggle-$eventOrder-suggestion"
        val event = EventRequest(eventName, mapOf("suggestion_index" to index.toString()))

        sendEventAsync(event)
    }

    fun sendManualSuggestionTrigger(renderingMode: RenderingMode) {
        val event = EventRequest("manual-suggestion-trigger", mapOf("suggestion_rendering_mode" to renderingMode.name))
        sendEventAsync(event)
    }

    fun sendSuggestionDropped(editor: Editor, suggestion: TabNineCompletion?, reason: SuggestionDroppedReason) {
        if (suggestion == null) return

        try {
            val filename = getFilename(FileDocumentManager.getInstance().getFile(editor.document))
            if (filename == null) {
                Logger.getInstance(javaClass).warn("Failed to obtain filename, skipping sending suggestion dropped with reason = $reason")
                return
            }
            val netLength = suggestion.netLength
            val metadata = suggestion.completionMetadata

            val event = SuggestionDroppedRequest(netLength, reason, filename, metadata)
            sendEventAsync(event)
        } catch (t: Throwable) {
            Logger.getInstance(javaClass).warn("Failed to send suggestion dropped with reason = $reason", t)
        }
    }

    private fun <R : BinaryResponse> sendEventAsync(event: BinaryRequest<R>) {
        AppExecutorUtil.getAppExecutorService().submit {
            binaryRequestFacade.executeRequest(event)
        }
    }
}
