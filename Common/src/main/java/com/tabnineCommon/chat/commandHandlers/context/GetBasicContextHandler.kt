package com.tabnineCommon.chat.commandHandlers.context

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.util.indexing.FileBasedIndex
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

    override fun handle(payload: Unit?, project: Project): BasicContext {
        return ReadAction.compute<BasicContext, Throwable> { createBasicContext(project) }
    }

    private fun createBasicContext(project: Project): BasicContext {
        val editor = getEditorFromProject(project) ?: return noEditorResponse(project)

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        val fileUri = psiFile?.virtualFile?.path
        val language = psiFile?.language?.id?.let { getPredominantWorkspaceLanguage() }

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
        return BasicContext(null, getPredominantWorkspaceLanguage(), metadata)
    }

    private fun getPredominantWorkspaceLanguage(maxFiles: Int = 50): String? {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return null
        val fileIndex = FileBasedIndex.getInstance()

        val languageCount = mutableMapOf<String, Int>()

        // Using the FileBasedIndex to iterate through files
        fileIndex.iterateIndexableFiles(
            { virtualFile ->
                if (!virtualFile.isDirectory && virtualFile.isValid) {
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    val language = psiFile?.language?.id ?: return@iterateIndexableFiles true

                    languageCount[language] = languageCount.getOrDefault(language, 0) + 1
                }
                true
            },
            project, null
        )

        // Sorting languages by frequency
        val sortedLanguages = languageCount.toList().sortedByDescending { (_, count) -> count }

        // Returning the most frequent language or null if no files are found
        return sortedLanguages.firstOrNull()?.first
    }
}
