package com.tabnineCommon.general

import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.capabilities.ISuggestionsModeService
import com.tabnineCommon.inline.InlineCompletionHandler
import com.tabnineCommon.selections.CompletionPreviewListener
import java.util.Optional

interface IProviderOfThings {
    val binaryRequestFacade: BinaryRequestFacade
    val suggestionsModeService: ISuggestionsModeService
    val completionsEventSender: CompletionsEventSender
    val inlineCompletionHandler: InlineCompletionHandler
    val completionPreviewListener: CompletionPreviewListener
    val tabnineBundleVersionUrl: Optional<String>
    fun getSubscriptionType(serviceLevel: ServiceLevel?): ISubscriptionType
}
