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
    val fileUri: String? = null,
    val language: String? = null,
    var metadata: JsonObject? = null
) {
    constructor(metadata: JsonObject?) : this() {
        this.metadata = metadata
    }
}

class GetBasicContextHandler(gson: Gson) : ChatMessageHandler<Unit, BasicContext>(gson) {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    override fun handle(payload: Unit?, project: Project): BasicContext {
        val editor = getEditorFromProject(project) ?: return noEditorResponse(project)

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        val fileUri = psiFile?.virtualFile?.path
        var metadata = fileUri?.let {
            binaryRequestFacade.executeRequest(FileMetadataRequest(it))
        }
        if (metadata?.has("error") == true) {
            metadata = null
        }
        val language = metadata?.get("language")?.asString ?: psiFile?.language?.id

        val basicContext = BasicContext(fileUri, language, metadata)
        BasicContextCache.save(editor, basicContext)

        return basicContext
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
