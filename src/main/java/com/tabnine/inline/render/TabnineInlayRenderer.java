package com.tabnine.inline.render;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.util.Disposer;
import com.tabnine.general.Utils;
import com.tabnine.prediction.TabNineCompletion;

import java.util.List;
import java.util.stream.Collectors;

public class TabnineInlayRenderer {
    private GenericInlay inline;
    private GenericInlay block;

    public GenericInlay getInline() {
        return inline;
    }

    public boolean hasInlays() {
        return this.inline != null || this.block != null;
    }

    public void register(Disposable parent) {
        if (inline != null) {
            Disposer.register(parent, inline.inner);
        }
        if (block != null) {
            Disposer.register(parent, block.inner);
        }
    }

    public void clear() {
        if (inline != null) {
            Disposer.dispose(inline.inner);
            inline = null;
        }

        if (block != null) {
            Disposer.dispose(block.inner);
            block = null;
        }
    }

    public void render(Editor editor, String suffix, TabNineCompletion completion, int offset) {
        List<String> lines = Utils.asLines(suffix);
        String firstLine = lines.get(0);
        List<String> otherLines = lines.stream().skip(1).collect(Collectors.toList());

        InlineElementRenderer inlineElementRenderer =
                new InlineElementRenderer(editor, firstLine, completion.deprecated);
        this.inline = new GenericInlay(editor
                .getInlayModel()
                .addInlineElement(offset, true, inlineElementRenderer));

        if (otherLines.size() > 0) {
            BlockElementRenderer blockElementRenderer =
                    new BlockElementRenderer(editor, otherLines, completion.deprecated);
            this.block = new GenericInlay(editor
                    .getInlayModel()
                    .addBlockElement(
                            offset,
                            true,
                            false,
                            1,
                            blockElementRenderer
                    ));
        }
    }
}
