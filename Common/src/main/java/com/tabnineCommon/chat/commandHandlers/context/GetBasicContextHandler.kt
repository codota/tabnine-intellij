package com.tabnineCommon.chat.commandHandlers.context

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiDocumentManager
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
}

public fun getPredominantWorkspaceLanguage(
    includeFilePredicate: (String) -> Boolean = { true }
): String? {
    val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return null
    val fileIndex = FileBasedIndex.getInstance()
    val maxFilesToConsider = 50

    val extensionCount = mutableMapOf<String, Int>()
    var filesProcessed = 0

    // Using the FileBasedIndex to iterate through files
    fileIndex.iterateIndexableFiles(
        { virtualFile ->
            if (filesProcessed >= maxFilesToConsider) {
                // If we've hit the limit, stop the iteration by returning false
                return@iterateIndexableFiles false
            }

            if (!virtualFile.isDirectory && virtualFile.isValid && includeFilePredicate(virtualFile.path)) {
                val fileExtension = virtualFile.extension?.toLowerCase() // Get the file extension

                if (fileExtension != null) {
                    extensionCount[fileExtension] = extensionCount.getOrDefault(fileExtension, 0) + 1
                    filesProcessed++ // Increment the counter
                }
            }
            true // Continue iteration
        },
        project, null
    )

    // Sorting languages by frequency
    val sortedExtensions = extensionCount.toList().sortedByDescending { (_, count) -> count }

    // Returning the most frequent language or null if no files are found
    return sortedExtensions.firstOrNull()?.first?.let { getLanguageFromExtension(it) }
}

val extensionToLanguageMap = mapOf(
    "abap" to "abap",
    "bat" to "bat",
    "bibtex" to "bib",
    "c" to "c",
    "clojure" to "clj",
    "coffeescript" to "coffee",
    "cpp" to "cpp",
    "csharp" to "cs",
    "css" to "css",
    "cuda-cpp" to "cu",
    "dart" to "dart",
    "diff" to "diff",
    "dockerfile" to "dockerfile",
    "fsharp" to "fs",
    "go" to "go",
    "groovy" to "groovy",
    "haml" to "haml",
    "handlebars" to "handlebars",
    "hlsl" to "hlsl",
    "html" to "html",
    "ini" to "ini",
    "jade" to "jade",
    "java" to "java",
    "javascript" to "js",
    "javascriptreact" to "jsx",
    "json" to "json",
    "julia" to "jl",
    "latex" to "tex",
    "less" to "less",
    "lua" to "lua",
    "makefile" to "make",
    "markdown" to "md",
    "objective-c" to "m",
    "objective-cpp" to "mm",
    "perl" to "pl",
    "perl6" to "6pl",
    "php" to "php",
    "plaintext" to "txt",
    "powershell" to "ps1",
    "pug" to "pug",
    "python" to "py",
    "r" to "r",
    "razor" to "cshtml",
    "ruby" to "rb",
    "rust" to "rs",
    "sass" to "sass",
    "scss" to "scss",
    "shaderlab" to "shader",
    "shellscript" to "sh",
    "slim" to "slim",
    "sql" to "sql",
    "stylus" to "styl",
    "swift" to "swift",
    "tex" to "tex",
    "typescript" to "ts",
    "typescriptreact" to "tsx",
    "vb" to "vb",
    "vue" to "vue",
    "xml" to "xml",
    "xsl" to "xsl",
    "yaml" to "yaml"
)

val languageFromExtension = extensionToLanguageMap.entries.associate { (k, v) -> v to k }
fun getLanguageFromExtension(extension: String): String? {
    return languageFromExtension[extension]
}
