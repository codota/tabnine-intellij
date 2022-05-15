package integration.uninstall

import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.uninstall.UninstallRequest
import com.tabnine.binary.requests.uninstall.UninstallResponse
import com.tabnine.general.TabnineZipFile
import com.tabnine.general.readTempTabninePluginZip
import com.tabnine.lifecycle.UninstallReporter
import io.mockk.every
import io.mockk.mockkStatic
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.time.Duration

val STALE_FILE_DURATION: Duration = Duration.ofMinutes(1)

class UninstallTestDriver {
    val uninstallReporterMock: UninstallReporter = Mockito.mock(UninstallReporter::class.java)
    val binaryRequestFacadeMock: BinaryRequestFacade = Mockito.mock(BinaryRequestFacade::class.java)

    fun mockUninstallResponse() {
        Mockito.`when`(binaryRequestFacadeMock.executeRequest(ArgumentMatchers.any(UninstallRequest::class.java)))
            .thenReturn(UninstallResponse())
    }

    fun verifyUninstallNotReported() {
        Mockito.verify(binaryRequestFacadeMock, Mockito.never())
            .executeRequest(ArgumentMatchers.any(UninstallRequest::class.java))
        Mockito.verify(uninstallReporterMock, Mockito.never()).reportUninstall(ArgumentMatchers.anyMap())
    }

    fun verifyUninstallRequestFired() {
        Mockito.verify(binaryRequestFacadeMock, Mockito.times(1))
            .executeRequest(ArgumentMatchers.any(UninstallRequest::class.java))
        Mockito.verify(uninstallReporterMock, Mockito.never()).reportUninstall(ArgumentMatchers.anyMap())
    }

    fun verifyUninstallReporterFallback() {
        Mockito.verify(binaryRequestFacadeMock, Mockito.times(1))
            .executeRequest(ArgumentMatchers.any(UninstallRequest::class.java))
        Mockito.verify(uninstallReporterMock, Mockito.times(1)).reportUninstall(ArgumentMatchers.anyMap())
    }

    fun mockExistingPluginZipFiles(filenames: List<String>? = null, stale: Boolean = false) {
        mockkStatic("com.tabnine.general.PluginsZipReaderKt")
        val creationTimeMillis = System.currentTimeMillis() - millisAgo(stale)
        every { readTempTabninePluginZip() }.returns(
            filenames?.let { TabnineZipFile(it, creationTimeMillis) }
        )
    }

    private fun millisAgo(stale: Boolean) = if (stale) {
        STALE_FILE_DURATION.toMillis() * 2
    } else {
        0
    }
}
