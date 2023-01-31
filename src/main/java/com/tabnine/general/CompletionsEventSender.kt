package com.tabnine.general

import com.intellij.util.concurrency.AppExecutorUtil
import com.tabnine.binary.BinaryRequest
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.BinaryResponse
import com.tabnine.binary.requests.analytics.EventRequest
import com.tabnine.binary.requests.autocomplete.CompletionMetadata
import com.tabnine.binary.requests.notifications.shown.SuggestionDroppedRequest
import com.tabnine.inline.CompletionOrder

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

    fun sendManualSuggestionTrigger() {
        val event = EventRequest("manual-suggestion-trigger", mapOf())
        sendEventAsync(event)
    }

    fun sendSuggestionDropped(netLength: Int, filename: String?, metadata: CompletionMetadata?) {
        val event = SuggestionDroppedRequest(netLength, filename, metadata)
        System.err.println(event)
        sendEventAsync(event)
    }

    private fun <R : BinaryResponse> sendEventAsync(event: BinaryRequest<R>) {
        AppExecutorUtil.getAppExecutorService().submit {
            binaryRequestFacade.executeRequest(event)
        }
    }
}
