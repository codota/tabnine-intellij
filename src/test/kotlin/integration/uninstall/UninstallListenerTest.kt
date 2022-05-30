package integration.uninstall

import com.intellij.openapi.extensions.PluginId
import com.tabnine.UninstallListener
import org.junit.Test

class UninstallListenerTest {
    private val driver = UninstallTestDriver()
    private val uninstallListener = UninstallListener(driver.binaryRequestFacadeMock, driver.uninstallReporterMock)

    @Test
    fun shouldNotReportUninstallWhenDescriptorIsNotOurs() {
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0", PluginId.getId("kaka")))

        driver.verifyUninstallNotReported()
    }

    @Test
    fun shouldNotReportUninstallWhenCalledFromInstallOrUpdatePlugin() {
        driver.callFromInstallOrUpdatePlugin { uninstallListener.uninstall(PluginDescriptorMock("0.0.0")) }
        driver.verifyUninstallNotReported()
    }

    @Test
    fun shouldReportUninstallWhenCalledNotFromInstallOrUpdatePlugin() {
        driver.mockUninstallResponse()
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))
        driver.verifyUninstallRequestFired()
    }

    @Test
    fun shouldFallbackToUninstallReporterWhenUninstallRequestFailed() {
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))
        driver.verifyUninstallReporterFallback()
    }
}
