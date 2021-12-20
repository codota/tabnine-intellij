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
    private GenericInlayWrapper inlineBeforeSuffix;
    private GenericInlayWrapper inlineAfterSuffix;
    private GenericInlayWrapper block;

    @Nullable
    public Integer getOffset() {
        if (inlineBeforeSuffix != null) {
            return inlineBeforeSuffix.inner().getOffset();
        }
        if (block != null) {
            return block.inner().getOffset();
        }

        return null;
    }

    @Nullable
    public Rectangle getBounds() {
        if (inlineBeforeSuffix != null) {
            return inlineBeforeSuffix.inner().getBounds();
        }
        if (block != null) {
            return block.inner().getBounds();
        }

        return null;
    }

    public boolean isEmpty() {
        return this.inlineBeforeSuffix == null && this.inlineAfterSuffix == null && this.block == null;
    }

    public void register(Disposable parent) {
        if (inlineBeforeSuffix != null) {
            Disposer.register(parent, inlineBeforeSuffix.inner());
        }
        if (inlineAfterSuffix != null) {
            Disposer.register(parent, inlineAfterSuffix.inner());
        }
        if (block != null) {
            Disposer.register(parent, block.inner());
        }
    }

    public void clear() {
        if (inlineBeforeSuffix != null) {
            Disposer.dispose(inlineBeforeSuffix.inner());
            inlineBeforeSuffix = null;
        }

        if (inlineAfterSuffix != null) {
            Disposer.dispose(inlineAfterSuffix.inner());
            inlineAfterSuffix = null;
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
            int endIndex = firstLine.indexOf(completion.oldSuffix);
            if (!completion.oldSuffix.isEmpty() && endIndex >= 0) {
                String before = firstLine.substring(0, endIndex);
                InlineElementRenderer beforeInline =
                        new InlineElementRenderer(editor, before, completion.deprecated);
                this.inlineBeforeSuffix = new GenericInlayWrapper(editor
                        .getInlayModel()
                        .addInlineElement(offset, true, beforeInline));

                int startOfAfter = endIndex + completion.oldSuffix.length();
                if (startOfAfter < firstLine.length()) {
                    String after = firstLine.substring(startOfAfter);

                    InlineElementRenderer afterInline =
                            new InlineElementRenderer(editor, after, completion.deprecated);
                    this.inlineAfterSuffix = new GenericInlayWrapper(editor
                            .getInlayModel()
                            .addInlineElement(offset + before.length(), true, afterInline));
                }
            } else {
            InlineElementRenderer inlineElementRenderer =
                    new InlineElementRenderer(editor, firstLine, completion.deprecated);
            this.inlineBeforeSuffix = new GenericInlayWrapper(editor
                    .getInlayModel()
                    .addInlineElement(offset, true, inlineElementRenderer));
            }
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
