
import com.intellij.psi.PsiManager
import com.tabnine.MockedBinaryCompletionTestCase
import com.tabnineCommon.capabilities.SuggestionsMode
import com.tabnineCommon.chat.commandHandlers.context.getPredominantWorkspaceLanguage
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.sql.Date
import java.text.SimpleDateFormat

class PredominantWorkspaceLanguageTest : MockedBinaryCompletionTestCase() {
    @Before
    fun init() {
        Mockito.`when`(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.HYBRID)
    }
    @Test
    fun `test getPredominantWorkspaceLanguage with various languages`() {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        val date = java.util.Date()
        val directoryName = formatter.format(date)
        createFileInProject("$directoryName/a.java")
        createFileInProject("$directoryName/b.java")

        val language = getPredominantWorkspaceLanguage { it.contains(directoryName) }
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
