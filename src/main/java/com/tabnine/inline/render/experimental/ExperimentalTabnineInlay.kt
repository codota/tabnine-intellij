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
    private var beforeSuffixInlay: Inlay<*>? = null
    private var afterSuffixInlay: Inlay<*>? = null
    private var blockInlay: Inlay<*>? = null

    override val offset: Int?
        get() = beforeSuffixInlay?.offset ?: afterSuffixInlay?.offset ?: blockInlay?.offset

    override fun getBounds(): Rectangle? {
        val result = beforeSuffixInlay?.bounds?.let { Rectangle(it) }

        result?.bounds?.let {
            afterSuffixInlay?.bounds?.let { after -> result.add(after) }
            blockInlay?.bounds?.let { blockBounds -> result.add(blockBounds) }
        }

        return result
    }

    override val isEmpty: Boolean
        get() = beforeSuffixInlay == null && afterSuffixInlay == null && blockInlay == null

    override fun register(parent: Disposable) {
        beforeSuffixInlay?.let {
            Disposer.register(parent, it)
        }
        afterSuffixInlay?.let {
            Disposer.register(parent, it)
        }
        blockInlay?.let {
            Disposer.register(parent, it)
        }
    }

    override fun clear() {
        beforeSuffixInlay?.let {
            Disposer.dispose(it)
            beforeSuffixInlay = null
        }
        afterSuffixInlay?.let {
            Disposer.dispose(it)
            afterSuffixInlay = null
        }
        blockInlay?.let {
            Disposer.dispose(it)
            blockInlay = null
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
                beforeSuffixInlay = renderInline(editor, beforeSuffix, completion, offset)

                val afterSuffixIndex = endIndex + completion.oldSuffix.length
                val after = if (afterSuffixIndex < firstLine.length) firstLine.substring(afterSuffixIndex) else null
                after?.let {
                    afterSuffixInlay = renderInline(editor, it, completion, offset + beforeSuffix.length)
                }
            } else {
                beforeSuffixInlay = renderInline(editor, firstLine, completion, offset)
            }
        }
        if (otherLines.size > 0) {
            blockInlay = renderBlock(editor, otherLines, completion, offset)
        }
    }

    private fun renderBlock(
        editor: Editor,
        otherLines: MutableList<String>,
        completion: TabNineCompletion,
        offset: Int
    ): Inlay<BlockElementRenderer>? {
        val blockElementRenderer = BlockElementRenderer(editor, otherLines, completion.deprecated)
        return editor
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
    ): Inlay<InlineElementRenderer>? {
        val inline = InlineElementRenderer(editor, before, completion.deprecated)
        return editor
            .inlayModel
            .addInlineElement(offset, true, inline)
    }
}
