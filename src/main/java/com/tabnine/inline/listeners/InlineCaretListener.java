package com.tabnine.inline.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.util.Disposer;
import com.tabnine.inline.CompletionPreview;
import org.jetbrains.annotations.NotNull;

public class InlineCaretListener implements CaretListener {
    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }

        CompletionPreview preview = CompletionPreview.findCompletionPreview(event.getEditor());
        if (preview == null || preview.isCurrentlyNotDisplayingInlays()) return;

        preview.clear();
    }
}
