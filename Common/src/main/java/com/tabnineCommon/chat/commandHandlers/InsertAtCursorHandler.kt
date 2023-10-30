import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffContext
import com.intellij.diff.contents.DiffContent
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.diff.tools.simple.SimpleDiffViewer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import javax.swing.JComponent

data class Diff(val comparableCode: String)
data class InsertPayload(val code: String, val diff: Diff?)

class InsertAtCursorHandler(gson: Gson) : ChatMessageHandler<InsertPayload, Unit>(gson) {
    override fun handle(payload: InsertPayload?, project: Project) {
        val code = payload?.code ?: return
        val editor = getEditorFromProject(project) ?: return
        val diff = payload.diff

        ApplicationManager.getApplication().invokeLater {
            if (diff == null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    val selectionModel = editor.selectionModel
                    editor.document.setReadOnly(false)
                    editor.document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, code)
                }
                return@invokeLater
            }

            val comparableCode = diff.comparableCode

            val comparableCodePosition = editor.document.text.indexOf(comparableCode)

            if (comparableCodePosition == -1) {
                Messages.showMessageDialog(
                    project,
                    "Could not insert the selected diff.",
                    "Tabnine Message",
                    Messages.getWarningIcon()
                )
                return@invokeLater
            }

            val shouldInsertText = InsertDiffDialog(project, comparableCode, code).showAndGet()

            if (shouldInsertText) {
                WriteCommandAction.runWriteCommandAction(project) {
                    val endPosition = comparableCodePosition + comparableCode.length
                    editor.document.setReadOnly(false)
                    editor.document.replaceString(comparableCodePosition, endPosition, code)
                }
            }
        }
    }

    override fun deserializeRequest(data: JsonElement?): InsertPayload? {
        return gson.fromJson(data, InsertPayload::class.java)
    }
}

class InsertDiffDialog(private val project: Project, before: String, after: String) :
    DialogWrapper(project) {
    private val before: DiffContent
    private val after: DiffContent

    init {
        title = "Tabnine Chat - Preview Changes"
        setOKButtonText("Apply")
        setCancelButtonText("Reject")
        isModal = true
        val diffContentFactory = DiffContentFactory.getInstance()
        this.before = diffContentFactory.create(project, before)
        this.after = diffContentFactory.create(project, after)
        init()
    }

    override fun createCenterPanel(): JComponent {
        val request = SimpleDiffRequest("Tabnine Chat - Preview Changes", before, after, "Before", "After")
        val simpleDiffViewer = SimpleDiffViewer(emptyDiffContext(project), request)
        simpleDiffViewer.init()
        simpleDiffViewer.rediff()
        return simpleDiffViewer.component
    }

    private fun emptyDiffContext(contextProject: Project): DiffContext {
        return object : DiffContext() {
            override fun isFocusedInWindow(): Boolean = false

            override fun requestFocusInWindow() {
            }

            override fun getProject(): Project {
                return contextProject
            }

            override fun isWindowFocused(): Boolean {
                return false
            }
        }
    }
}
