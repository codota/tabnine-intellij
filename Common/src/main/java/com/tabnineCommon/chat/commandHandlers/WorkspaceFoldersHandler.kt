import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.lifecycle.WorkspaceListenerService

data class WorkspaceFoldersPayload(private val rootPaths: List<String>)

class WorkspaceFoldersHandler(gson: Gson) : ChatMessageHandler<Unit, WorkspaceFoldersPayload>(gson) {
    private val workspaceService = ServiceManager.getService(WorkspaceListenerService::class.java)

    override fun handle(payload: Unit?, project: Project): WorkspaceFoldersPayload? {
        val rootPaths = workspaceService.getWorkspaceRootPaths() ?: return null
        return WorkspaceFoldersPayload(rootPaths)
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
