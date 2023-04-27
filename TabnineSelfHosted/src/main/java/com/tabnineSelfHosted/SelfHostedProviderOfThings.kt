package com.tabnineSelfHosted

import com.tabnineCommon.binary.BinaryProcessGatewayProvider
import com.tabnineCommon.binary.BinaryProcessRequesterProvider
import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.BinaryRun
import com.tabnineCommon.binary.fetch.BinaryDownloader
import com.tabnineCommon.binary.fetch.BinaryRemoteSource
import com.tabnineCommon.binary.fetch.BinaryValidator
import com.tabnineCommon.binary.fetch.BinaryVersionFetcher
import com.tabnineCommon.binary.fetch.BundleDownloader
import com.tabnineCommon.binary.fetch.GeneralDownloader
import com.tabnineCommon.binary.fetch.LocalBinaryVersions
import com.tabnineCommon.binary.fetch.TempBinaryValidator
import com.tabnineCommon.binary.fetch.TempBundleValidator
import com.tabnineCommon.capabilities.ISuggestionsModeService
import com.tabnineCommon.general.CompletionsEventSender
import com.tabnineCommon.general.IProviderOfThings
import com.tabnineCommon.general.ISubscriptionType
import com.tabnineCommon.general.ServiceLevel
import com.tabnineCommon.inline.InlineCompletionHandler
import com.tabnineCommon.prediction.CompletionFacade
import com.tabnineCommon.selections.CompletionPreviewListener

class SelfHostedProviderOfThings : IProviderOfThings {
    companion object {
        val INSTANCE: SelfHostedProviderOfThings by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SelfHostedProviderOfThings() }
        private val _suggestionsModeService = SuggestionsModeService()
    }

    private var _binaryRequestFacade: BinaryRequestFacade? = null
    override val binaryRequestFacade: BinaryRequestFacade
        get() {
            if (this.serverUrl == null) {
                throw IllegalArgumentException("serverUrl is null")
            }

            if (this._binaryRequestFacade == null) {
                this._binaryRequestFacade = BinaryRequestFacade(
                    BinaryProcessRequesterProvider.create(
                        BinaryRun(this.instanceOfBinaryFetcher(serverUrl!!)),
                        BinaryProcessGatewayProvider(),
                        serverUrl,
                        60_000
                    )
                )
            }

            return this._binaryRequestFacade!!
        }
    override val suggestionsModeService: ISuggestionsModeService
        get() = _suggestionsModeService

    private var _completionsEventSender: CompletionsEventSender? = null

    override val completionsEventSender: CompletionsEventSender
        get() {
            if (_completionsEventSender == null) {
                _completionsEventSender = CompletionsEventSender(binaryRequestFacade)
            }

            return _completionsEventSender!!
        }

    private var _inlineCompletionHandler: InlineCompletionHandler? = null
    override val inlineCompletionHandler: InlineCompletionHandler
        get() {
            if (_inlineCompletionHandler == null) {
                _inlineCompletionHandler = InlineCompletionHandler(
                    CompletionFacade(
                        binaryRequestFacade, suggestionsModeService
                    ),
                    binaryRequestFacade,
                    suggestionsModeService
                )
            }

            return _inlineCompletionHandler!!
        }
    override val completionPreviewListener: CompletionPreviewListener
        get() = TODO("Not yet implemented")

    private var serverUrl: String? = null

    fun setServerUrl(serverUrl: String?) {
        this.serverUrl = serverUrl
    }

    override fun getSubscriptionType(serviceLevel: ServiceLevel?): ISubscriptionType {
        return EnterpriseSubscriptionType.Enterprise
    }

    private fun instanceOfBinaryFetcher(serverUrl: String): BinaryVersionFetcher {
        return BinaryVersionFetcher(
            LocalBinaryVersions(BinaryValidator()), BinaryRemoteSource(),
            BinaryDownloader(TempBinaryValidator(BinaryValidator()), GeneralDownloader()),
            BundleDownloader(TempBundleValidator(), GeneralDownloader()),
            serverUrl
        )
    }
}
