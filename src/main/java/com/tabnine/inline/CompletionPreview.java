package com.tabnine.inline;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.general.DependencyContainer;
import com.tabnine.inline.render.TabnineInlay;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.selections.AutoImporter;
import com.tabnine.selections.CompletionPreviewListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class CompletionPreview implements Disposable, EditorMouseMotionListener {

    private static final Key<CompletionPreview> INLINE_COMPLETION_PREVIEW =
            Key.create("INLINE_COMPLETION_PREVIEW");
    public static final List<Integer> EDITOR_ACTION_KEYS = Arrays.asList(KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_Y, KeyEvent.VK_V, KeyEvent.VK_C, KeyEvent.VK_D);
    private static final int HINT_DELAY_MS = 100;

    private final CompletionPreviewListener previewListener =
            DependencyContainer.instanceOfCompletionPreviewListener();
    private final Editor editor;
    private final PsiFile file;
    private final Alarm alarm;
    private List<TabNineCompletion> completions;
    private int previewIndex;
    private String suffix;
    private final TabnineInlay tabnineInlay;
    private final KeyListener previewKeyListener = new PreviewKeyListener();
    private final CaretListener caretMoveListener;
    private final AtomicBoolean inApplyMode = new AtomicBoolean(false);

    private CompletionPreview(@NotNull Editor editor, @NotNull PsiFile file) {
        this.tabnineInlay = TabnineInlay.create();
        this.editor = editor;
        this.file = file;
        this.alarm = new Alarm(this);
        caretMoveListener =
                new CaretListener() {
                    @Override
                    public void caretPositionChanged(@NotNull CaretEvent event) {
                        if (ApplicationManager.getApplication().isUnitTestMode()) {
                            return;
                        }

                        clear();
                    }
                };
        ObjectUtils.consumeIfCast(
                editor,
                EditorEx.class,
                e ->
                        e.addFocusListener(
                                new FocusChangeListener() {
                                    @Override
                                    public void focusGained(@NotNull Editor editor) {
                                    }

                                    @Override
                                    public void focusLost(@NotNull Editor editor) {
//                                        clear();
                                    }
                                }));
    }

    @Nullable
    String updatePreview(@NotNull List<TabNineCompletion> completions, int previewIndex, int offset) {
        if (SuggestionsMode.getSuggestionMode() != SuggestionsMode.INLINE) {
            return null;
        }
        this.completions = completions;
        this.previewIndex = previewIndex;
        TabNineCompletion completion = completions.get(previewIndex);
        this.tabnineInlay.clear();

        suffix = completion.getSuffix();

        if (!suffix.isEmpty()
                && editor instanceof EditorImpl
                && !editor.getSelectionModel().hasSelection()
                && InplaceRefactoring.getActiveInplaceRenamer(editor) == null) {
            editor.getDocument().startGuardedBlockChecking();

            try {
                tabnineInlay.render(this.editor, this.suffix, completion, offset);
            } finally {
                editor.getDocument().stopGuardedBlockChecking();
            }
            if (!tabnineInlay.isEmpty()) {
                tabnineInlay.register(this);
                registerListeners();
            }
        }
        return suffix;
    }

    private void registerListeners() {
        editor.getCaretModel().addCaretListener(caretMoveListener);
        editor.getContentComponent().addKeyListener(previewKeyListener);
        editor.addEditorMouseMotionListener(this);
    }

    void clear() {
        if (inApplyMode.get()) {
            return;
        }

        if (!tabnineInlay.isEmpty()) {
            tabnineInlay.clear();
            editor.removeEditorMouseMotionListener(this);
        }

        editor.getContentComponent().removeKeyListener(previewKeyListener);
        editor.getCaretModel().removeCaretListener(caretMoveListener);

        completions = null;
        suffix = null;
    }

    @Override
    public void dispose() {
        clear();
        editor.putUserData(INLINE_COMPLETION_PREVIEW, null);
        CompletionState.clearCompletionState(editor);
    }

    @Override
    public void mouseMoved(@NotNull EditorMouseEvent e) {
        alarm.cancelAllRequests();
        if (tabnineInlay.isEmpty()) {
            return;
        }
        if (e.getArea() == EditorMouseEventArea.EDITING_AREA) {
            MouseEvent mouseEvent = e.getMouseEvent();
            Point point = mouseEvent.getPoint();
            if (isOverPreview(point)) {
                alarm.addRequest(
                        () -> {
                            Point p =
                                    SwingUtilities.convertPoint(
                                            (Component) mouseEvent.getSource(),
                                            point,
                                            editor.getComponent().getRootPane().getLayeredPane());
                            InlineHints.showPreInsertionHint(editor, p);
                        },
                        HINT_DELAY_MS);
            }
        }
    }

    private boolean isOverPreview(@NotNull Point p) {
        try {
            Rectangle bounds = tabnineInlay.getBounds();
            if (bounds != null) {
                return bounds.contains(p);
            }
        } catch (Throwable e) {
            // swallow
        }
        LogicalPosition pos = editor.xyToLogicalPosition(p);
        int line = pos.line;

        if (line >= editor.getDocument().getLineCount()) return false;

        int pointOffset = editor.logicalPositionToOffset(pos);
        Integer inlayOffset = tabnineInlay.getOffset();
        if (inlayOffset == null) {
            return false;
        }

        return pointOffset >= inlayOffset && pointOffset <= inlayOffset + suffix.length();
    }

    @Override
    public void mouseDragged(@NotNull EditorMouseEvent e) {
    }

    @Nullable
    public Integer getStartOffset() {
        return tabnineInlay.getOffset();
    }

    void applyPreview() {
        inApplyMode.set(true);
        TabnineDocumentListener.mute();
        Integer renderedOffset = tabnineInlay.getOffset();
        if (renderedOffset == null) {
            return;
        }

        try {
            TabNineCompletion currentCompletion = completions.get(previewIndex);
            int startOffset = renderedOffset - currentCompletion.completionPrefix.length();
            int endOffset = renderedOffset + suffix.length();
            if (currentCompletion.oldSuffix != null && !currentCompletion.oldSuffix.trim().isEmpty()) {
                int deletingEndOffset = renderedOffset + currentCompletion.oldSuffix.length();
                editor.getDocument().deleteString(renderedOffset, deletingEndOffset);
            }
            editor.getDocument().insertString(renderedOffset, suffix);
            editor.getCaretModel().moveToOffset(endOffset);
            AutoImporter.registerTabNineAutoImporter(editor, file.getProject(), startOffset, endOffset);
            previewListener.previewSelected(
                    new CompletionPreviewListener.CompletionPreviewData(completions, previewIndex, file));
            inApplyMode.set(false);
            Disposer.dispose(CompletionPreview.this);
        } catch (Throwable e) {
            Logger.getInstance(getClass()).warn("Error on committing the renderedOffset completion", e);
        } finally {
            inApplyMode.set(false);
            TabnineDocumentListener.unmute();
        }
    }

    @NotNull
    static CompletionPreview findOrCreateCompletionPreview(
            @NotNull Editor editor, @NotNull PsiFile file) {
        CompletionPreview preview = findCompletionPreview(editor);
        if (preview == null) {
            preview = new CompletionPreview(editor, file);
            EditorUtil.disposeWithEditor(editor, preview);
            editor.putUserData(INLINE_COMPLETION_PREVIEW, preview);
        }
        return preview;
    }

    @Nullable
    static CompletionPreview findCompletionPreview(@NotNull Editor editor) {
        return editor.getUserData(INLINE_COMPLETION_PREVIEW);
    }

    static void disposeIfExists(@NotNull Editor editor) {
        disposeIfExists(editor, preview -> true);
    }

    static void disposeIfExists(
            @NotNull Editor editor, @NotNull Predicate<CompletionPreview> condition) {
        CompletionPreview preview = findCompletionPreview(editor);
        if (preview != null && condition.test(preview)) {
            Disposer.dispose(preview);
        }
    }

    @TestOnly
    public static String getPreviewText(@NotNull Editor editor) {
        CompletionPreview preview = editor.getUserData(INLINE_COMPLETION_PREVIEW);
        if (preview != null) {
            return preview.suffix;
        }
        return null;
    }

    private class PreviewKeyListener extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent event) {
            try {
                if (CompletionPreview.this.tabnineInlay.isEmpty()) {
                    return;
                }
                int keyCode = event.getKeyCode();
                boolean isEditorAction = (event.isMetaDown() || event.isControlDown()) && EDITOR_ACTION_KEYS.contains(keyCode);
                if (keyCode == KeyEvent.VK_BACK_SPACE
                        || keyCode == KeyEvent.VK_DELETE
                        || isEditorAction) {
                    Disposer.dispose(CompletionPreview.this);
                }
            } catch (Throwable err) {
                Logger.getInstance(getClass()).warn("Error in Tabnine preview KeyListener", err);
            }
        }
    }
}
