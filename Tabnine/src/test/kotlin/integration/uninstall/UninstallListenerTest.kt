package integration.uninstall

import com.intellij.openapi.extensions.PluginId
import com.tabnineCommon.UninstallListener
import org.junit.Test

class UninstallListenerTest {
    private val driver = UninstallTestDriver()
    private val uninstallListener = UninstallListener(driver.binaryRequestFacadeMock, driver.uninstallReporterMock)

    @Test
    fun shouldNotReportUninstallWhenDescriptorIsNotOurs() {
        driver.callFromUninstallAndUpdateUi {
            uninstallListener.uninstall(
                PluginDescriptorMock(
                    "0.0.0",
                    PluginId.getId("kaka")
                )
            )
        }

        driver.verifyUninstallNotReported()
    }

    @Test
    fun shouldNotReportUninstallWhenNotCalledFromUninstallAndUpdateUi() {
        uninstallListener.uninstall(PluginDescriptorMock("0.0.0"))
        driver.verifyUninstallNotReported()
    }

    @Test
    fun shouldReportUninstallWhenCalledFromUninstallAndUpdateUi() {
        driver.mockUninstallResponse()
        driver.callFromUninstallAndUpdateUi { uninstallListener.uninstall(PluginDescriptorMock("0.0.0")) }
        driver.verifyUninstallRequestFired()
    }

    @Test
    fun shouldFallbackToUninstallReporterWhenUninstallRequestFailed() {
        driver.callFromUninstallAndUpdateUi { uninstallListener.uninstall(PluginDescriptorMock("0.0.0")) }
        driver.verifyUninstallReporterFallback()
    }
}
