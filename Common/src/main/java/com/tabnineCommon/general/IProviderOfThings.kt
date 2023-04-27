package com.tabnineCommon.general

import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.capabilities.ISuggestionsModeService
import com.tabnineCommon.inline.InlineCompletionHandler
import com.tabnineCommon.selections.CompletionPreviewListener

interface IProviderOfThings {
    val binaryRequestFacade: BinaryRequestFacade
    val suggestionsModeService: ISuggestionsModeService
    val completionsEventSender: CompletionsEventSender
    val inlineCompletionHandler: InlineCompletionHandler
    val completionPreviewListener: CompletionPreviewListener
    fun getSubscriptionType(serviceLevel: ServiceLevel?): ISubscriptionType
}
