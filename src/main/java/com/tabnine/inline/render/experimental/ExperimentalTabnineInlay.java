package com.tabnine.inline.render.experimental;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Disposer;
import com.tabnine.general.Utils;
import com.tabnine.inline.render.GenericInlayWrapper;
import com.tabnine.inline.render.TabnineInlay;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ExperimentalTabnineInlay implements TabnineInlay {
    private GenericInlayWrapper inline;
    private GenericInlayWrapper block;

    @Nullable
    public Integer getOffset() {
        if (inline != null) {
            return inline.inner().getOffset();
        }
        if (block != null) {
            return block.inner().getOffset();
        }

        return null;
    }

    @Nullable
    public Rectangle getBounds() {
        if (inline != null) {
            return inline.inner().getBounds();
        }
        if (block != null) {
            return block.inner().getBounds();
        }

        return null;
    }

    public boolean isEmpty() {
        return this.inline == null && this.block == null;
    }

    public void register(Disposable parent) {
        if (inline != null) {
            Disposer.register(parent, inline.inner());
        }
        if (block != null) {
            Disposer.register(parent, block.inner());
        }
    }

    public void clear() {
        if (inline != null) {
            Disposer.dispose(inline.inner());
            inline = null;
        }

        if (block != null) {
            Disposer.dispose(block.inner());
            block = null;
        }
    }

    public void render(Editor editor, @NotNull String suffix, TabNineCompletion completion, int offset) {
        List<String> lines = Utils.asLines(suffix);
        String firstLine = lines.get(0);
        List<String> otherLines = lines.stream().skip(1).collect(Collectors.toList());

        if (!firstLine.isEmpty()) {
            InlineElementRenderer inlineElementRenderer =
                    new InlineElementRenderer(editor, firstLine, completion.deprecated);
            this.inline = new GenericInlayWrapper(editor
                    .getInlayModel()
                    .addInlineElement(offset, true, inlineElementRenderer));
        }
        if (otherLines.size() > 0) {
            BlockElementRenderer blockElementRenderer =
                    new BlockElementRenderer(editor, otherLines, completion.deprecated);
            this.block = new GenericInlayWrapper(editor
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
