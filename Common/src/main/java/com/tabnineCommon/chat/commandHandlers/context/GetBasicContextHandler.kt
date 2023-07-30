package com.tabnineCommon.chat.commandHandlers.context

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.tabnineCommon.binary.requests.fileMetadata.FileMetadataRequest
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.general.DependencyContainer
import java.io.File

data class BasicContext(
    private val fileUri: String? = null,
    private val language: String? = null,
    private var metadata: JsonObject? = null
) {
    constructor(metadata: JsonObject?) : this() {
        this.metadata = metadata
    }
}

class GetBasicContextHandler(gson: Gson) : ChatMessageHandler<Unit, BasicContext>(gson) {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    override fun handle(payload: Unit?, project: Project): BasicContext? {
        val editor = getEditorFromProject(project) ?: return noEditorResponse(project)

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        val fileUri = psiFile?.virtualFile?.path
        val language = psiFile?.language?.id

        var metadata = if (fileUri != null) binaryRequestFacade.executeRequest(FileMetadataRequest(fileUri)) else null

        if (metadata?.has("error") == true) {
            metadata = null
        }

        return BasicContext(fileUri, language, metadata)
    }

    override fun deserializeRequest(data: JsonElement?) {}

    private fun noEditorResponse(project: Project): BasicContext {
        val firstFileInProject = project.basePath?.let { basePath -> File(basePath).walk().find { it.isFile } }
        var metadata =
            if (firstFileInProject != null) binaryRequestFacade.executeRequest(FileMetadataRequest(firstFileInProject.path)) else null

        if (metadata?.has("error") == true) {
            metadata = null
        }

        return BasicContext(metadata)
    }
}
