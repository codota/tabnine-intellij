
import com.intellij.psi.PsiManager
import com.tabnine.MockedBinaryCompletionTestCase
import com.tabnineCommon.capabilities.SuggestionsMode
import com.tabnineCommon.chat.commandHandlers.context.getPredominantWorkspaceLanguage
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class PredominantWorkspaceLanguageTest : MockedBinaryCompletionTestCase() {
    @Before
    fun init() {
        Mockito.`when`(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.HYBRID)
    }

    @Test
    fun `test getPredominantWorkspaceLanguage with various languages`() {
        // Assuming we have a helper function to create files in the test environment:
        createFileInProject("a.java")
        createFileInProject("b.java")

        val language = getPredominantWorkspaceLanguage { it.contains("src") }
        assertEquals("Kotlin", language)
    }

    @Test
    fun `test getPredominantWorkspaceLanguage returns null when no projects`() {
        val language = getPredominantWorkspaceLanguage { it.contains("no_src") }
        assertNull(language)
    }

    private fun createFileInProject(name: String) {
        val file = myFixture.configureByText(name, "")
        val f = PsiManager.getInstance(project).findFile(file.virtualFile)
        assertNotNull(f)
    }
}
