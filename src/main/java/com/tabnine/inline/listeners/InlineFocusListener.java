package com.tabnine.inline.listeners;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.tabnine.inline.CompletionPreview;
import org.jetbrains.annotations.NotNull;

public class InlineFocusListener implements FocusChangeListener {
    @Override
    public void focusGained(@NotNull Editor editor) {
    }

    @Override
    public void focusLost(@NotNull Editor editor) {
        CompletionPreview preview = CompletionPreview.findCompletionPreview(editor);
        if (preview == null || preview.isCurrentlyNotDisplayingInlays()) return;

//        preview.clear();
    }
}
