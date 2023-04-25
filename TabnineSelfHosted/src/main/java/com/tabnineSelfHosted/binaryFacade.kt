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

class SelfHostedBinaryFacade {
    companion object {
        val INSTANCE: SelfHostedBinaryFacade by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SelfHostedBinaryFacade() }
    }

    private var binaryRequestFacade: BinaryRequestFacade? = null
    fun getRequestFacade(serverUrl: String?): BinaryRequestFacade {
        if (serverUrl == null) {
            throw IllegalArgumentException("serverUrl is null")
        }
        if (this.binaryRequestFacade == null) {
            this.binaryRequestFacade = BinaryRequestFacade(
                BinaryProcessRequesterProvider.create(
                    BinaryRun(this.instanceOfBinaryFetcher(serverUrl)),
                    BinaryProcessGatewayProvider(),
                    serverUrl,
                    60_000
                )
            )
        }

        return this.binaryRequestFacade!!
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
