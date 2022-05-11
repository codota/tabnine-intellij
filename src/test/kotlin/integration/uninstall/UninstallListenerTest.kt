package integration.uninstall

import com.intellij.openapi.extensions.PluginId
import com.tabnine.UninstallListener
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.uninstall.UninstallRequest
import com.tabnine.binary.requests.uninstall.UninstallResponse
import com.tabnine.general.readTempTabninePluginZip
import com.tabnine.lifecycle.UninstallReporter
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class UninstallListenerTest {
    private val uninstallReporterMock: UninstallReporter = mock(UninstallReporter::class.java)
    private var binaryRequestFacadeMock: BinaryRequestFacade = mock(BinaryRequestFacade::class.java)
    private val uninstallListener = UninstallListener(
        binaryRequestFacadeMock,
        uninstallReporterMock
    )

    @Test
    fun shouldNotReportUninstallWhenDescriptorIsNotOurs() {
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0", PluginId.getId("kaka")))

        verifyUninstallNotReported()
    }

    @Test
    fun shouldNotReportUninstallWhenZipVersionIsNewerThenCurrent() {
        mockExistingPluginZipFiles(listOf("TabNine-0.0.1.jar"))

        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        verifyUninstallNotReported()
    }

    @Test
    fun shouldNotReportUninstallWhenZipHasOlderAndNewerVersions() {
        mockExistingPluginZipFiles(listOf("TabNine-0.0.0.jar", "TabNine-0.0.2.jar"))
        uninstallListener.uninstall(PluginDescriptorMock("0.0.1"))

        verifyUninstallNotReported()
    }

    @Test
    fun shouldFireUninstallRequestWhenZipVersionIsOlderThanCurrent() {
        mockExistingPluginZipFiles(listOf("TabNine-0.0.0.jar"))
        `when`(binaryRequestFacadeMock.executeRequest(any(UninstallRequest::class.java))).thenReturn(UninstallResponse())

        uninstallListener.uninstall(PluginDescriptorMock("0.0.1"))

        verifyUninstallRequestFired()
    }

    @Test
    fun shouldFireUninstallRequestWhenZipDoesNotContainOurPlugin() {
        mockExistingPluginZipFiles(listOf("NotTabNine-0.0.1.jar"))
        `when`(binaryRequestFacadeMock.executeRequest(any(UninstallRequest::class.java))).thenReturn(UninstallResponse())

        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        verifyUninstallRequestFired()
    }

    @Test
    fun shouldFireUninstallRequestWhenZipVersionIsNotAValidSemver() {
        mockExistingPluginZipFiles(listOf("TabNine-0.a.1.jar"))
        `when`(binaryRequestFacadeMock.executeRequest(any(UninstallRequest::class.java))).thenReturn(UninstallResponse())

        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        verifyUninstallRequestFired()
    }

    @Test
    fun shouldFireUninstallRequestWhenZipVersionNotExists() {
        mockExistingPluginZipFiles()
        `when`(binaryRequestFacadeMock.executeRequest(any(UninstallRequest::class.java))).thenReturn(UninstallResponse())

        uninstallListener.uninstall(PluginDescriptorMock("0.0.1"))

        verifyUninstallRequestFired()
    }

    @Test
    fun shouldFallbackToUninstallReporterWhenFailedToSendRequest() {
        mockExistingPluginZipFiles(listOf("TabNine-0.0.1.zip"))
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        verifyUninstallReporterFallback()
    }

    private fun verifyUninstallNotReported() {
        verify(binaryRequestFacadeMock, never()).executeRequest(any(UninstallRequest::class.java))
        verify(uninstallReporterMock, never()).reportUninstall(anyMap())
    }

    private fun verifyUninstallRequestFired() {
        verify(binaryRequestFacadeMock, times(1)).executeRequest(any(UninstallRequest::class.java))
        verify(uninstallReporterMock, never()).reportUninstall(anyMap())
    }

    private fun verifyUninstallReporterFallback() {
        verify(binaryRequestFacadeMock, times(1)).executeRequest(any(UninstallRequest::class.java))
        verify(uninstallReporterMock, times(1)).reportUninstall(anyMap())
    }

    private fun mockExistingPluginZipFiles(filenames: List<String>? = null) {
        mockkStatic("com.tabnine.general.PluginsZipReaderKt")
        every { readTempTabninePluginZip() }.returns(filenames)
    }
}
