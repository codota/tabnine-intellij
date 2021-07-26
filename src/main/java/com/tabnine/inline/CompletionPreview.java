package com.tabnine.inline;

import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;
import com.intellij.ui.JBColor;
import com.intellij.util.containers.FList;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

class CompletionPreview implements Disposable {

    private static final Key<CompletionPreview> INLINE_COMPLETION_PREVIEW = Key.create("INLINE_COMPLETION_PREVIEW");

    private final Editor editor;
    private String suffix;
    private Inlay inlay;
    private final KeyListener previewKeyListener = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent event) {
            try {
                if (inlay == null) {
                    return;
                }
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                    CompletionPreview.this.clear();
                    Disposer.dispose(CompletionPreview.this);
                    return;
                }
                if (event.getKeyCode() != KeyEvent.VK_RIGHT) {
                    return;
                }
                applyPreview();
            } catch (Throwable err) {
                Logger.getInstance(getClass()).warn("Error in Tabnine preview KeyListener", err);
            }
        }
    };

    private CompletionPreview(@NotNull Editor editor) {
        this.editor = editor;
    }

    void applyPreview() {
        WriteCommandAction.runWriteCommandAction(editor.getProject(), "Tabnine Inline Completion", null, () -> {
            TabnineDocumentListener.mute();
            try {
                editor.getDocument().insertString(inlay.getOffset(), suffix);
                editor.getCaretModel().moveToOffset(inlay.getOffset() + suffix.length());
                Disposer.dispose(CompletionPreview.this);
            } finally {
                TabnineDocumentListener.unmute();
            }
        });
    }

    @Nullable
    String updatePreview(@NotNull TabNineCompletion completion, int offset) {
        System.out.println("--> updatePreview with offset=" + offset + ", itemText=" + completion.newPrefix + completion.newSuffix);
        if (Registry.is("ide.lookup.preview.insertion")) {
            // Don't override jetbrains built in feature if it is on
            return null;
        }
        if (inlay != null) {
            Disposer.dispose(inlay);
            inlay = null;
        }
        suffix = getSuffixText(completion);

        if (!suffix.isEmpty() && editor instanceof EditorImpl &&
                !editor.getSelectionModel().hasSelection() &&
                InplaceRefactoring.getActiveInplaceRenamer(editor) == null) {
            inlay = editor.getInlayModel().addInlineElement(offset, true, createGrayRenderer(suffix));
            if (inlay != null) {
                Disposer.register(this, inlay);
                editor.getContentComponent().addKeyListener(previewKeyListener);
            }
        }
        return suffix;
    }

    void clear() {
        editor.getContentComponent().removeKeyListener(previewKeyListener);
        if (inlay != null) {
            Disposer.dispose(inlay);
            inlay = null;
        }
        suffix = null;
    }

    private String getSuffixText(@NotNull TabNineCompletion completion) {
        String itemText = completion.newPrefix + completion.newSuffix;
        String prefix = completion.completionPrefix;
        if (prefix.isEmpty()) {
            return itemText;
        }

        FList<TextRange> fragments = LookupCellRenderer.getMatchingFragments(prefix, itemText);
        if (fragments != null && !fragments.isEmpty()) {
            List<TextRange> list = new ArrayList<>(fragments);
            return itemText.substring(list.get(list.size() - 1).getEndOffset());
        }
        return "";
    }

    @NotNull
    private EditorCustomElementRenderer createGrayRenderer(final String suffix) {
        return new EditorCustomElementRenderer() {
            @Override
            public int calcWidthInPixels(@NotNull Inlay inlay) {
                return editor.getContentComponent().getFontMetrics(getFont(editor)).stringWidth(suffix);
            }

            @Override
            public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
                g.setColor(JBColor.GRAY);
                g.setFont(getFont(editor));
                g.drawString(suffix, targetRegion.x, targetRegion.y + ((EditorImpl) editor).getAscent());
            }

            private Font getFont(@NotNull Editor editor) {
                return editor.getColorsScheme().getFont(EditorFontType.PLAIN);
            }
        };
    }

    @Override
    public void dispose() {
        clear();
        editor.putUserData(INLINE_COMPLETION_PREVIEW, null);
    }

    static CompletionPreview findOrCreateCompletionPreview(@NotNull Editor editor) {
        CompletionPreview preview = editor.getUserData(INLINE_COMPLETION_PREVIEW);
        if (preview == null) {
            preview = new CompletionPreview(editor);
            EditorUtil.disposeWithEditor(editor, preview);
            editor.putUserData(INLINE_COMPLETION_PREVIEW, preview);
        }
        return preview;
    }
}
