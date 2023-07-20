package com.tabnineCommon.chat.commandHandlers.utils

import com.intellij.ide.util.gotoByName.ChooseByNameModel
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider
import com.intellij.ide.util.gotoByName.GotoSymbolModel2
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

data class Symbol(
    val name: String,
    val text: String,
    val absolutePath: String,
    val relativePath: String
) {
    companion object {
        fun createFromElement(element: PsiElement, projectPathString: String): Symbol? {
            val elementPathString = element.containingFile.virtualFile.path
            if (!elementPathString.startsWith(projectPathString)) {
                Logger.getInstance(Symbol::class.java)
                    .warn("Path $elementPathString doesn't start with $projectPathString")
                return null
            }
            try {
                val projectPath = Paths.get(projectPathString)
                val elementPath = Paths.get(elementPathString)
                val relativePath = projectPath.relativize(elementPath)

                val name = getName(element)
                if (name == null) {
                    Logger.getInstance(Symbol::class.java).warn("Couldn't find name for $element")
                    return null
                }

                return Symbol(name, element.text, elementPathString, relativePath.toString())
            } catch (e: Throwable) {
                Logger.getInstance(Symbol::class.java).warn("Couldn't create symbol from $element", e)
                return null
            }
        }

        /**
         * Using reflection to get the name of a PsiElement - by calling getName() on the PsiElement.
         * This is done because the actual elements returned from the search are language-specific elements,
         * and in order to use them directly, this project needs to be dependent on certain language-specific plugins.
         *
         * Since all of them should have a getName() method, I decided to use reflection instead.
         * I guess we'll see in the logs if it doesn't work ¯\_(ツ)_/¯
         */
        private fun getName(element: PsiElement): String? =
            element::class.java.methods.find { it.name == "getName" }?.invoke(element) as String?
    }
}

object SymbolsResolver {
    fun resolveSymbols(project: Project, document: Document, text: String, maxListSize: Int): List<Symbol> {
        Logger.getInstance(javaClass).debug("Resolving symbols for $text")
        val projectPath = project.basePath
        if (projectPath == null) {
            Logger.getInstance(javaClass).warn("Couldn't find project path")
            return emptyList()
        }

        val context = PsiDocumentManager.getInstance(project).getPsiFile(document)
        val symbols = mutableSetOf<Symbol>()
        val millis = measureTimeMillis {
            DefaultChooseByNameItemProvider(context).filterElements(
                TabnineChooseByNameViewModel(project, maxListSize), "*$text*", false,
                ProgressIndicatorBase(),
                Processor { element ->
                    val psiElement = element as PsiElement
                    Symbol.createFromElement(psiElement, projectPath)?.let { symbols.add(it) }

                    symbols.size < maxListSize
                }
            )
        }
        Logger.getInstance(javaClass).debug("Found ${symbols.size} elements for text $text in $millis ms")

        return symbols.toList()
    }
}

private class TabnineChooseByNameViewModel(private val project: Project, private val maxListSize: Int) :
    ChooseByNameViewModel {
    private val model = GotoSymbolModel2(project)

    override fun getProject(): Project {
        return project
    }

    override fun getModel(): ChooseByNameModel {
        return model
    }

    override fun isSearchInAnyPlace(): Boolean {
        return false
    }

    override fun transformPattern(pattern: String): String {
        return pattern
    }

    override fun canShowListForEmptyPattern(): Boolean {
        return false
    }

    override fun getMaximumListSizeLimit(): Int {
        return maxListSize
    }
}
