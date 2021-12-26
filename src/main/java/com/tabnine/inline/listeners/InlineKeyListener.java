package com.tabnine.inline.listeners;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Disposer;
import com.tabnine.inline.CompletionPreview;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InlineKeyListener extends KeyAdapter {
    private final Editor editor;

    public InlineKeyListener(Editor editor) {
        this.editor = editor;
    }

    @Override
    public void keyReleased(KeyEvent event) {
        CompletionPreview preview = CompletionPreview.findCompletionPreview(editor);
        if (preview == null || preview.isCurrentlyNotDisplayingInlays()) return;

        int key = event.getKeyCode();
        // do not interfere with inline shortcuts
        if (key == KeyEvent.VK_ALT
                || key == KeyEvent.VK_OPEN_BRACKET
                || key == KeyEvent.VK_CLOSE_BRACKET
                || key == KeyEvent.VK_TAB
        )
            return;

        try {
            Disposer.dispose(preview);
        } catch (Throwable err) {
            Logger.getInstance(getClass()).warn("Error in Tabnine preview KeyListener", err);
        }
    }
}
