package integration.uninstall

import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.uninstall.UninstallRequest
import com.tabnine.binary.requests.uninstall.UninstallResponse
import com.tabnine.lifecycle.UninstallReporter
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class UninstallTestDriver {
    val uninstallReporterMock: UninstallReporter = Mockito.mock(UninstallReporter::class.java)
    val binaryRequestFacadeMock: BinaryRequestFacade = Mockito.mock(BinaryRequestFacade::class.java)

    fun mockUninstallResponse() {
        Mockito.`when`(binaryRequestFacadeMock.executeRequest(ArgumentMatchers.any(UninstallRequest::class.java)))
            .thenReturn(UninstallResponse())
    }

    fun callFromInstallOrUpdatePlugin(callback: () -> Unit) {
        installOrUpdatePlugin(callback)
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

    private fun installOrUpdatePlugin(callback: () -> Unit) {
        callback()
    }
}
