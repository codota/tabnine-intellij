
import com.intellij.codeInsight.hints.CollectorWithSettings
import com.intellij.codeInsight.hints.InlayHintsSinkImpl
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.openapi.editor.Inlay
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase
import com.tabnineCommon.chat.lens.TabnineLensJavaBaseProvider
import org.junit.Test

class TabnineLensIntegrationTest : LightPlatformCodeInsightFixture4TestCase() {

    @Test
    fun `should show inlay hints for java function when chat is enabled`() {
        setJavaFile()
        val provider = TabnineLensJavaBaseProvider { true }
        runCollector(provider)
        val inlays = getRenderedInlays()

        assertEquals(inlays[0].offset, 0)
        assertEquals(inlays[1].offset, 20)
        assertEquals(inlays.size, 2)
    }

    @Test
    fun `should not show inlay hints for java function when chat is disabled`() {
        setJavaFile()
        val provider = TabnineLensJavaBaseProvider { false }
        runCollector(provider)
        val inlays = getRenderedInlays()

        assertEquals(inlays.size, 0)
    }

    private fun setJavaFile() {
        myFixture.configureByText(
            "Test.java",
            "public class Test {\n  public void test() {\n    System.out.println(\"Hello World\");\n  }\n}"
        )
    }

    private fun runCollector(provider: TabnineLensJavaBaseProvider) {
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
    }

    private fun getRenderedInlays(): MutableList<Inlay<*>> {
        return myFixture.editor.inlayModel.getBlockElementsInRange(
            myFixture.file.textRange.startOffset,
            myFixture.file.textRange.endOffset
        )
    }
}
