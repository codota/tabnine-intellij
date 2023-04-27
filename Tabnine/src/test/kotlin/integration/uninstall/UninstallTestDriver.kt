package integration.uninstall

import com.tabnineCommon.binary.BinaryRequestFacade
import com.tabnineCommon.binary.requests.uninstall.UninstallRequest
import com.tabnineCommon.binary.requests.uninstall.UninstallResponse
import com.tabnineCommon.lifecycle.UninstallReporter
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class UninstallTestDriver {
    val uninstallReporterMock: UninstallReporter = Mockito.mock(
        UninstallReporter::class.java
    )
    val binaryRequestFacadeMock: BinaryRequestFacade = Mockito.mock(
        BinaryRequestFacade::class.java
    )

    fun mockUninstallResponse() {
        Mockito.`when`(binaryRequestFacadeMock.executeRequest(ArgumentMatchers.any(UninstallRequest::class.java)))
            .thenReturn(UninstallResponse())
    }

    fun callFromUninstallAndUpdateUi(callback: () -> Unit) {
        uninstallAndUpdateUi(callback)
    }

    fun verifyUninstallNotReported() {
        Mockito.verify(binaryRequestFacadeMock, Mockito.never())
            .executeRequest(ArgumentMatchers.any(UninstallRequest::class.java))
        Mockito.verify(uninstallReporterMock, Mockito.never()).reportUninstall(ArgumentMatchers.anyMap(), ArgumentMatchers.isNull())
    }

    fun verifyUninstallRequestFired() {
        Mockito.verify(binaryRequestFacadeMock, Mockito.times(1))
            .executeRequest(ArgumentMatchers.any(UninstallRequest::class.java))
        Mockito.verify(uninstallReporterMock, Mockito.never()).reportUninstall(ArgumentMatchers.anyMap(), ArgumentMatchers.isNull())
    }

    fun verifyUninstallReporterFallback() {
        Mockito.verify(binaryRequestFacadeMock, Mockito.times(1))
            .executeRequest(ArgumentMatchers.any(UninstallRequest::class.java))
        Mockito.verify(uninstallReporterMock, Mockito.times(1)).reportUninstall(ArgumentMatchers.anyMap(), ArgumentMatchers.isNull())
    }

    private fun uninstallAndUpdateUi(callback: () -> Unit) {
        callback()
    }
}
