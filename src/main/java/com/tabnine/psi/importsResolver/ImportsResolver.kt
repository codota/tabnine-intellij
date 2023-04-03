package com.tabnine.psi.importsResolver

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.tabnine.psi.LocationLink
import com.tabnine.psi.Position
import com.tabnine.psi.Range
import java.net.URI

abstract class ImportsResolver {
    abstract fun potentialElementsPredicate(element: PsiElement): Boolean

    fun resolveImportsLinksInFile(psiFile: PsiFile): List<LocationLink> {
        return psiFile.children
            ?.filter { potentialElementsPredicate(it) }
            ?.flatMap { resolveImportReference(it) } ?: emptyList()
    }
}

fun resolveImportsLinksInFile(editor: Editor): List<LocationLink> {
    val psiFile = editor.project
        ?.let { PsiDocumentManager.getInstance(it).getPsiFile(editor.document) }
        ?: return emptyList()
    val resolver = getImportsResolver(psiFile) ?: return emptyList()
    return resolver.resolveImportsLinksInFile(psiFile)
}

private fun resolveImportReference(element: PsiElement): List<LocationLink> {
    val referee = element.reference?.resolve()
        ?: return element.children?.flatMap { resolveImportReference(it) } ?: emptyList()

    if (!(referee.isValid && referee.isWritable)) return emptyList()
    val filePath = referee.containingFile?.virtualFile?.url?.let(URI::create) ?: return emptyList()
    val range = referee.textRange ?: return emptyList()
    val text = referee.containingFile?.text ?: return emptyList()
    val linesAccumulativeLengths = linesAccumulativeLengths(text)

    val startPos = toLogicalPosition(linesAccumulativeLengths, range.startOffset) ?: return emptyList()
    val endPos = toLogicalPosition(linesAccumulativeLengths, range.endOffset) ?: return emptyList()

    return listOf(LocationLink(filePath, Range(startPos, endPos)))
}

private fun toLogicalPosition(linesAccumulativeLengths: List<Int>, offset: Int): Position? {
    val line = linesAccumulativeLengths.indexOfFirst { it >= offset }

    if (line == -1) return null
    if (line == 0) return Position(0, offset)

    val column = offset - 1 - linesAccumulativeLengths[line - 1]
    return Position(line, column)
}

private fun linesAccumulativeLengths(text: String): List<Int> {
    val linesAccumulativeLengths = mutableListOf<Int>()
    var index = text.indexOf('\n')

    while (index != -1) {
        linesAccumulativeLengths.add(index)
        val nextIndex = text.indexOf('\n', index + 1)
        index = nextIndex
    }

    linesAccumulativeLengths.add(text.length)

    return linesAccumulativeLengths
}
