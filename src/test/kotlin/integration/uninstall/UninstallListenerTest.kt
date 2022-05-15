package integration.uninstall

import com.intellij.openapi.extensions.PluginId
import com.tabnine.UninstallListener
import org.junit.Test

class UninstallListenerTest {
    private val driver = UninstallTestDriver()
    private val uninstallListener = UninstallListener(
        driver.binaryRequestFacadeMock,
        driver.uninstallReporterMock,
        STALE_FILE_DURATION
    )

    @Test
    fun shouldNotReportUninstallWhenDescriptorIsNotOurs() {
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0", PluginId.getId("kaka")))

        driver.verifyUninstallNotReported()
    }

    @Test
    fun shouldNotReportUninstallWhenZipVersionIsNewerThenCurrent() {
        driver.mockExistingPluginZipFiles(listOf("TabNine-0.0.1.jar"))

        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        driver.verifyUninstallNotReported()
    }

    @Test
    fun shouldNotReportUninstallWhenNewerZipVersionIsAlphaRelease() {
        driver.mockExistingPluginZipFiles(listOf("TabNine-0.0.1-alpha.20220504091432.jar"))

        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        driver.verifyUninstallNotReported()
    }

    @Test
    fun shouldFireUninstallRequestWhenNewerZipVersionIsNotAValidAlphaRelease() {
        driver.mockExistingPluginZipFiles(listOf("TabNine-0.0.1-alpha20220504091432.jar"))
        driver.mockUninstallResponse()
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        driver.verifyUninstallRequestFired()
    }

    @Test
    fun shouldNotReportUninstallWhenZipHasOlderAndNewerVersions() {
        driver.mockExistingPluginZipFiles(listOf("TabNine-0.0.0.jar", "TabNine-0.0.2.jar"))
        uninstallListener.uninstall(PluginDescriptorMock("0.0.1"))

        driver.verifyUninstallNotReported()
    }

    @Test
    fun shouldFireUninstallRequestWhenZipVersionIsOlderThanCurrent() {
        driver.mockExistingPluginZipFiles(listOf("TabNine-0.0.0.jar"))
        driver.mockUninstallResponse()

        uninstallListener.uninstall(PluginDescriptorMock("0.0.1"))

        driver.verifyUninstallRequestFired()
    }

    @Test
    fun shouldFireUninstallRequestWhenZipDoesNotContainOurPlugin() {
        driver.mockExistingPluginZipFiles(listOf("NotTabNine-0.0.1.jar"))
        driver.mockUninstallResponse()

        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        driver.verifyUninstallRequestFired()
    }

    @Test
    fun shouldFireUninstallRequestWhenZipVersionIsNotAValidSemver() {
        driver.mockExistingPluginZipFiles(listOf("TabNine-0.a.1.jar"))
        driver.mockUninstallResponse()

        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        driver.verifyUninstallRequestFired()
    }

    @Test
    fun shouldFireUninstallRequestWhenZipVersionNotExists() {
        driver.mockExistingPluginZipFiles()
        driver.mockUninstallResponse()

        uninstallListener.uninstall(PluginDescriptorMock("0.0.1"))

        driver.verifyUninstallRequestFired()
    }

    @Test
    fun shouldFallbackToUninstallReporterWhenFailedToSendRequest() {
        driver.mockExistingPluginZipFiles(listOf("TabNine-0.0.1.zip"))
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        driver.verifyUninstallReporterFallback()
    }

    @Test
    fun shouldFireUninstallRequestWhenNewerZipIsStale() {
        driver.mockExistingPluginZipFiles(listOf("TabNine-0.0.1.zip"), stale = true)
        driver.mockUninstallResponse()

        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))

        driver.verifyUninstallRequestFired()
    }
}
