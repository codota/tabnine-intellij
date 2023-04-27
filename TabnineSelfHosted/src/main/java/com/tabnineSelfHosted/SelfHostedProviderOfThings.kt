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
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.hover.HoverUpdater
import com.tabnineCommon.inline.InlineCompletionHandler
import com.tabnineCommon.prediction.CompletionFacade
import com.tabnineCommon.selections.CompletionPreviewListener
import com.tabnineCommon.statusBar.StatusBarUpdater
import java.util.Optional
import java.util.function.Supplier

class SelfHostedProviderOfThings : IProviderOfThings {
    companion object {
        val INSTANCE: SelfHostedProviderOfThings by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SelfHostedProviderOfThings() }
        private val _suggestionsModeService = SuggestionsModeService()
    }

    private var _binaryRequestFacade: BinaryRequestFacade? = null
    override val binaryRequestFacade: BinaryRequestFacade
        get() {
            if (this._binaryRequestFacade == null) {
                this._binaryRequestFacade = BinaryRequestFacade(
                    BinaryProcessRequesterProvider.create(
                        BinaryRun(this.instanceOfBinaryFetcher()),
                        BinaryProcessGatewayProvider(),
                        this.serverUrl.get(),
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
        get() {
            return CompletionPreviewListener(
                binaryRequestFacade, StatusBarUpdater(binaryRequestFacade), HoverUpdater()
            )
        }
    override val tabnineBundleVersionUrl: Optional<String>
        get() {
            return Optional.ofNullable<String>(System.getProperty(StaticConfig.REMOTE_VERSION_URL_PROPERTY))
                .or(Supplier { getBundleServerUrl().map { s: String -> "$s/version" } })
        }

    private var _serverUrl: String? = null
    override var serverUrl: Optional<String>
        get() {
            if (this._serverUrl.isNullOrBlank()) {
                throw IllegalStateException("serverUrl is null or Blank :(")
            }

            return Optional.of(this._serverUrl!!)
        }
        set(value) {
            this._serverUrl = value.orElse(null)
        }

    private fun getBundleServerUrl(): Optional<String> {
        return Optional.of(
            Optional.ofNullable(System.getProperty(StaticConfig.REMOTE_BASE_URL_PROPERTY))
                .orElse("${this.serverUrl}/bundles")
        )
    }

    override fun getSubscriptionType(serviceLevel: ServiceLevel?): ISubscriptionType {
        return EnterpriseSubscriptionType.Enterprise
    }

    private fun instanceOfBinaryFetcher(): BinaryVersionFetcher {
        return BinaryVersionFetcher(
            LocalBinaryVersions(BinaryValidator()), BinaryRemoteSource(),
            BinaryDownloader(TempBinaryValidator(BinaryValidator()), GeneralDownloader()),
            BundleDownloader(TempBundleValidator(), GeneralDownloader())
        )
    }
}
