package com.tabnineCommon.general

import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.capabilities.ISuggestionsModeService
import com.tabnineCommon.inline.InlineCompletionHandler
import com.tabnineCommon.inline.TabnineInlineLookupListener
import com.tabnineCommon.lifecycle.IBinaryInstantiatedActions
import com.tabnineCommon.prediction.CompletionFacade
import com.tabnineCommon.selections.TabNineLookupListener
import com.tabnineCommon.statusBar.CompletionPreviewListener
import java.util.Optional

interface IProviderOfThings {
    val tabnineInlineLookupListener: TabnineInlineLookupListener
    val tabNineLookupListener: TabNineLookupListener
    val completionFacade: CompletionFacade
    val binaryRequestFacade: BinaryRequestFacade
    val actionVisitor: IBinaryInstantiatedActions
    val suggestionsModeService: ISuggestionsModeService
    val completionsEventSender: CompletionsEventSender
    val inlineCompletionHandler: InlineCompletionHandler
    val completionPreviewListener: CompletionPreviewListener
    val tabnineBundleVersionUrl: Optional<String>
    val bundlesServerUrl: Optional<String>
    var serverUrl: Optional<String>
    fun getSubscriptionType(serviceLevel: ServiceLevel?): ISubscriptionType
}
