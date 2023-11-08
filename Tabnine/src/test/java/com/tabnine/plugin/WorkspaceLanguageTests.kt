
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
        createFileInProject("get_language/a.java")
        createFileInProject("get_language/b.java")

        val language = getPredominantWorkspaceLanguage { it.contains("get_language") }
        assertEquals("Kotlin", language)
    }

    @Test
    fun `test getPredominantWorkspaceLanguage returns null when no projects`() {
        val language = getPredominantWorkspaceLanguage { it.contains("no_src") }
        assertNull(language)
    }

    private fun createFileInProject(filePath: String) {
        val file = myFixture.addFileToProject(filePath, "")
        val f = PsiManager.getInstance(project).findFile(file.virtualFile)
        assertNotNull(f)
    }
}
