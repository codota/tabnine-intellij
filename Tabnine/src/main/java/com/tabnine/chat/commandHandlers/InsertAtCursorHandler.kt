import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.tabnine.chat.commandHandlers.ChatMessageHandler

data class InsertPayload(val code: String)

class InsertAtCursorHandler(gson: Gson) : ChatMessageHandler<InsertPayload, Unit>(gson) {
    override fun handle(payload: InsertPayload?, project: Project) {
        val code = payload?.code ?: return
        val editor = getEditorFromProject(project) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            val selectionModel = editor.selectionModel
            editor.document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, code)
        }
    }

    override fun deserializeRequest(data: JsonElement?): InsertPayload? {
        return gson.fromJson(data, InsertPayload::class.java)
    }
}
