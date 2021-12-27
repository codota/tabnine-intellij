package com.tabnine.inline.render.experimental

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.util.Disposer
import com.tabnine.general.Utils
import com.tabnine.inline.render.TabnineInlay
import com.tabnine.prediction.TabNineCompletion
import java.awt.Rectangle
import java.util.stream.Collectors

class ExperimentalTabnineInlay : TabnineInlay {
    private var inlineBeforeSuffix: Inlay<*>? = null
    private var inlineAfterSuffix: Inlay<*>? = null
    private var block: Inlay<*>? = null

    override val offset: Int?
        get() = inlineBeforeSuffix?.offset ?: block?.offset

    override val bounds: Rectangle?
        get() = inlineBeforeSuffix?.bounds ?: block?.bounds

    override val isEmpty: Boolean
        get() = inlineBeforeSuffix == null && inlineAfterSuffix == null && block == null

    override fun register(parent: Disposable) {
        inlineBeforeSuffix?.let {
            Disposer.register(parent, it)
        }
        inlineAfterSuffix?.let {
            Disposer.register(parent, it)
        }
        block?.let {
            Disposer.register(parent, it)
        }
    }

    override fun clear() {
        inlineBeforeSuffix?.let {
            Disposer.dispose(it)
            inlineBeforeSuffix = null
        }
        inlineAfterSuffix?.let {
            Disposer.dispose(it)
            inlineAfterSuffix = null
        }
        block?.let {
            Disposer.dispose(it)
            block = null
        }
    }

    override fun render(editor: Editor, suffix: String, completion: TabNineCompletion, offset: Int) {
        val lines = Utils.asLines(suffix)
        val firstLine = lines[0]
        val otherLines = lines.stream().skip(1).collect(Collectors.toList())
        if (firstLine.isNotEmpty()) {
            val endIndex = firstLine.indexOf(completion.oldSuffix)
            if (completion.oldSuffix.isNotEmpty() && endIndex > 0) {
                val beforeSuffix = firstLine.substring(0, endIndex)
                renderInline(editor, beforeSuffix, completion, offset)

                val afterSuffixIndex = endIndex + completion.oldSuffix.length
                val after = if (afterSuffixIndex < firstLine.length) firstLine.substring(afterSuffixIndex) else null
                after?.let {
                    renderInline(editor, it, completion, offset + beforeSuffix.length)
                }
            } else {
                renderInline(editor, firstLine, completion, offset)
            }
        }
        if (otherLines.size > 0) {
            renderBlock(editor, otherLines, completion, offset)
        }
    }

    private fun renderBlock(
        editor: Editor,
        otherLines: MutableList<String>,
        completion: TabNineCompletion,
        offset: Int
    ) {
        val blockElementRenderer = BlockElementRenderer(editor, otherLines, completion.deprecated)
        block = editor
            .inlayModel
            .addBlockElement(
                offset,
                true,
                false,
                1,
                blockElementRenderer
            )
    }

    private fun renderInline(
        editor: Editor,
        before: String,
        completion: TabNineCompletion,
        offset: Int
    ) {
        val inline = InlineElementRenderer(editor, before, completion.deprecated)
        inlineBeforeSuffix = editor
            .inlayModel
            .addInlineElement(offset, true, inline)
    }
}
