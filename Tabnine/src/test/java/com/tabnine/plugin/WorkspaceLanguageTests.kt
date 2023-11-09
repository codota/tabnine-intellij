
import com.intellij.psi.PsiManager
import com.tabnine.MockedBinaryCompletionTestCase
import com.tabnineCommon.chat.commandHandlers.context.getPredominantWorkspaceLanguage
import org.junit.Test
import java.text.SimpleDateFormat

class PredominantWorkspaceLanguageTest : MockedBinaryCompletionTestCase() {
    @Test
    fun `test getPredominantWorkspaceLanguage with various languages`() {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        val date = java.util.Date()
        val directoryName = "varius_languages_${formatter.format(date)}"
        createFileInProject("$directoryName/a.go")
        createFileInProject("$directoryName/b.go")
        createFileInProject("$directoryName/c.go")
        createFileInProject("$directoryName/d.java")
        createFileInProject("$directoryName/e.java")
        createFileInProject("$directoryName/f.kotlin")
        createFileInProject("$directoryName/g.kt")

        val language = getPredominantWorkspaceLanguage { it.contains(directoryName) }
        assertEquals("go", language)
    }

    @Test
    fun `test getPredominantWorkspaceLanguage with unknown language`() {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        val date = java.util.Date()
        val directoryName = "unknown_language_${formatter.format(date)}"
        createFileInProject("$directoryName/a.bla")
        createFileInProject("$directoryName/b.bla")
        createFileInProject("$directoryName/c.bla")

        val language = getPredominantWorkspaceLanguage { it.contains(directoryName) }
        assertNull(language)
    }

    @Test
    fun `test getPredominantWorkspaceLanguage with unknown language and known files`() {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        val date = java.util.Date()
        val directoryName = "unknown_language_known_files_${formatter.format(date)}"
        createFileInProject("$directoryName/a.bla")
        createFileInProject("$directoryName/b.bla")
        createFileInProject("$directoryName/c.bla")
        createFileInProject("$directoryName/c.go")

        val language = getPredominantWorkspaceLanguage { it.contains(directoryName) }
        assertEquals("go", language)
    }

    @Test
    fun `test getPredominantWorkspaceLanguage returns null when no projects`() {
        val language = getPredominantWorkspaceLanguage { it.contains("return_null") }
        assertNull(language)
    }

    private fun createFileInProject(filePath: String) {
        val file = myFixture.addFileToProject(filePath, "println(\"Hello, world!\")\n")
        val f = PsiManager.getInstance(project).findFile(file.virtualFile)
        assertNotNull(f)
    }
}
