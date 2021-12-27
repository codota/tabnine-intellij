package com.tabnine.inline.render.preserved;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Disposer;
import com.tabnine.inline.render.GenericInlayWrapper;
import com.tabnine.inline.render.TabnineInlay;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DefaultTabnineInlay implements TabnineInlay {
    private GenericInlayWrapper inlay;

    @Override
    public Integer getOffset() {
        if (inlay != null) {
            return inlay.inner().getOffset();
        }

        return null;
    }

    @Override
    public Rectangle getBounds() {
        if (inlay != null) {
            return inlay.inner().getBounds();
        }

        return null;
    }

    @Override
    public boolean isEmpty() {
        return inlay == null;
    }

    @Override
    public void register(Disposable parent) {
        if (inlay != null) {
            Disposer.register(parent, inlay.inner());
        }
    }

    @Override
    public void clear() {
        if (inlay != null) {
            Disposer.dispose(inlay.inner());
            inlay = null;
        }
    }

    @Override
    public void render(Editor editor, @NotNull String suffix, TabNineCompletion completion, int offset) {
        InlayElementRenderer inlayElementRenderer = new InlayElementRenderer(editor, suffix, completion.deprecated);
        inlay = new GenericInlayWrapper(editor
                .getInlayModel()
                .addInlineElement(offset, true, inlayElementRenderer));
    }
}
