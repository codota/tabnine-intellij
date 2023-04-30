package com.tabnineSelfHosted

import com.intellij.openapi.diagnostic.Logger
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
import com.tabnineCommon.inline.TabnineInlineLookupListener
import com.tabnineCommon.lifecycle.IBinaryInstantiatedActions
import com.tabnineCommon.prediction.CompletionFacade
import com.tabnineCommon.selections.TabNineLookupListener
import com.tabnineCommon.statusBar.CompletionPreviewListener
import com.tabnineCommon.statusBar.StatusBarUpdater
import com.tabnineSelfHosted.lifecycle.BinaryInstantiatedActions
import java.util.Optional
import java.util.function.Supplier

class SelfHostedProviderOfThings : IProviderOfThings {
    companion object {
        val INSTANCE: SelfHostedProviderOfThings by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SelfHostedProviderOfThings() }
        private val _suggestionsModeService = SuggestionsModeService()
    }

    override val tabnineInlineLookupListener: TabnineInlineLookupListener
        get() = TabnineInlineLookupListener()
    override val tabNineLookupListener: TabNineLookupListener
        get() {
            return TabNineLookupListener(
                binaryRequestFacade,
                StatusBarUpdater(binaryRequestFacade),
                suggestionsModeService
            )
        }
    override val completionFacade: CompletionFacade
        get() {
            return CompletionFacade(
                binaryRequestFacade, suggestionsModeService
            )
        }

    private var _binaryRequestFacade: BinaryRequestFacade? = null
    override val binaryRequestFacade: BinaryRequestFacade
        get() {
            if (this._binaryRequestFacade == null) {
                this._binaryRequestFacade = BinaryRequestFacade(
                    BinaryProcessRequesterProvider.create(
                        BinaryRun(this.instanceOfBinaryFetcher()),
                        BinaryProcessGatewayProvider(),
                        60_000
                    )
                )
            }

            return this._binaryRequestFacade!!
        }
    override val actionVisitor: IBinaryInstantiatedActions
        get() = BinaryInstantiatedActions()

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
                    completionFacade,
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
                .or(Supplier { bundlesServerUrl.map { s: String -> "$s/version" } })
        }
    override val bundlesServerUrl: Optional<String>
        get() {
            return Optional.of(
                Optional.ofNullable(System.getProperty(StaticConfig.REMOTE_BASE_URL_PROPERTY))
                    .orElse("${this.serverUrl}/bundles")
            )
        }

    private var _serverUrl: String? = null
    override var serverUrl: Optional<String>
        get() {
            if (this._serverUrl.isNullOrBlank()) {
                Logger.getInstance(SelfHostedProviderOfThings::class.java).warn("We don't have server URL, yet???")
                Optional.empty<String>()
            }

            return Optional.of(this._serverUrl!!)
        }
        set(value) {
            this._serverUrl = value.orElse(null)
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
