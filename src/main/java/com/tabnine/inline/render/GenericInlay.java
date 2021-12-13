package com.tabnine.inline.render;

import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;

/**
 * A thin wrapper to get rid of the exhaustive type definition of `Inlay<? extends EditorCustomElementRenderer>`
 */
public class GenericInlay {
    public final Inlay<? extends EditorCustomElementRenderer> inner;

    public GenericInlay(Inlay<? extends EditorCustomElementRenderer> inlay) {
        this.inner = inlay;
    }
}
