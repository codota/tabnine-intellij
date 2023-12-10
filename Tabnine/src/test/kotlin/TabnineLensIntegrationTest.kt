
import com.intellij.codeInsight.hints.CollectorWithSettings
import com.intellij.codeInsight.hints.InlayHintsSinkImpl
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase
import com.intellij.testFramework.replaceService
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.lens.TabnineLensJavaProvider
import org.junit.Test

class TabnineLensIntegrationTest : LightPlatformCodeInsightFixture4TestCase() {

    @Test
    fun `should return inlay hints for java function`() {
        ApplicationManager.getApplication().replaceService(
            CapabilitiesService::class.java,
            object : CapabilitiesService() {
                override fun isCapabilityEnabled(capability: Capability): Boolean {
                    return when (capability) {
                        Capability.TABNINE_CHAT -> true
                        else -> false
                    }
                }
            },
            testRootDisposable
        )

        myFixture.configureByText(
            "Test.java",
            "public class Test {\n  public void test() {\n    System.out.println(\"Hello World\");\n  }\n}"
        )

        val provider = TabnineLensJavaProvider()

        val file = myFixture.file
        val editor = myFixture.editor
        val sink = InlayHintsSinkImpl(editor)

        val collector = provider.getCollectorFor(file, editor, NoSettings(), sink)
        val collectorWithSettings = CollectorWithSettings(collector, provider.key, file.language, sink)
        collectorWithSettings.collectTraversingAndApply(
            editor,
            file,
            true
        )
        val blockElementsInRange = myFixture.editor.inlayModel.getBlockElementsInRange(
            file.textRange.startOffset,
            file.textRange.endOffset
        )

        assertEquals(blockElementsInRange.get(0).offset, 0)
        assertEquals(blockElementsInRange.get(1).offset, 20)
        assertEquals(blockElementsInRange.size, 2)
    }
}
