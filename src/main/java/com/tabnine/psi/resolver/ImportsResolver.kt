package com.tabnine.psi.resolver

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

    fun resolveImportsLinks(psiFile: PsiFile): List<LocationLink> {
        return psiFile?.children
            ?.filter { potentialElementsPredicate(it) }
            ?.flatMap { resolveImportReference(it) } ?: emptyList()
    }
}

fun resolveImportsLinks(editor: Editor): List<LocationLink> {
    val psiFile = editor.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(editor.document) } ?: return emptyList()
    val resolver = getResolver(psiFile) ?: return emptyList()
    return resolver.resolveImportsLinks(psiFile)
}

private fun resolveImportReference(element: PsiElement): List<LocationLink> {
    element.reference?.let {
        it.resolve()?.let { r ->
            if (!(r.isValid && r.isWritable)) return emptyList()
            val filePath = r.containingFile?.virtualFile?.url?.let { file -> URI.create(file) } ?: return emptyList()
            val range = r.textRange ?: return emptyList()
            val text = r.containingFile?.text ?: return emptyList()
            val linesLengths = linesAccumulativeLengths(text)
            val startPos = toLogicalPosition(linesLengths, range.startOffset) ?: return emptyList()
            val endPos = toLogicalPosition(linesLengths, range.endOffset) ?: return emptyList()

            return listOf(LocationLink(filePath, Range(startPos, endPos)))
        }
    }

    return element.children?.flatMap { resolveImportReference(it) } ?: emptyList()
}

private fun toLogicalPosition(linesLengths: List<Int>, offset: Int): Position? {
    val line = linesLengths.indexOfFirst { it >= offset }
    if (line == -1) {
        return null
    }
    if (line == 0) {
        return Position(0, offset)
    }
    val column = offset - 1 - linesLengths[line - 1]
    return Position(line, column)
}

private fun linesAccumulativeLengths(text: String): List<Int> {
    val linesLengths = mutableListOf<Int>()
    var index = text.indexOf('\n')
    while (index != -1) {
        linesLengths.add(index)
        val nextIndex = text.indexOf('\n', index + 1)
        index = nextIndex
    }
    linesLengths.add(text.length)
    return linesLengths
}
