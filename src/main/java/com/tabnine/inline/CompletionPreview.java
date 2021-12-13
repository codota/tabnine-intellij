package com.tabnine.inline;

import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.FList;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.general.DependencyContainer;
import com.tabnine.inline.render.GenericInlay;
import com.tabnine.inline.render.TabnineInlayRenderer;
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
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class CompletionPreview implements Disposable, EditorMouseMotionListener {

  private static final Key<CompletionPreview> INLINE_COMPLETION_PREVIEW =
      Key.create("INLINE_COMPLETION_PREVIEW");
  private static final int HINT_DELAY_MS = 100;

  private final CompletionPreviewListener previewListener =
      DependencyContainer.instanceOfCompletionPreviewListener();
  private final Editor editor;
  private final PsiFile file;
  private final Alarm alarm;
  private List<TabNineCompletion> completions;
  private int previewIndex;
  private String suffix;
  private final TabnineInlayRenderer tabnineInlayRenderer;
  private final KeyListener previewKeyListener = new PreviewKeyListener();
  private final CaretListener caretMoveListener;
  private final AtomicBoolean inApplyMode = new AtomicBoolean(false);

  private CompletionPreview(@NotNull Editor editor, @NotNull PsiFile file) {
    this.tabnineInlayRenderer = new TabnineInlayRenderer();
    this.editor = editor;
    this.file = file;
    alarm = new Alarm(this);
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
                  public void focusGained(@NotNull Editor editor) {}

                  @Override
                  public void focusLost(@NotNull Editor editor) {
                    clear();
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
    tabnineInlayRenderer.clear();

    suffix = getSuffixText(completion);

    if (!suffix.isEmpty()
        && editor instanceof EditorImpl
        && !editor.getSelectionModel().hasSelection()
        && InplaceRefactoring.getActiveInplaceRenamer(editor) == null) {
      editor.getDocument().startGuardedBlockChecking();

      try {
        tabnineInlayRenderer.render(this.editor, this.suffix, completion, offset);
      } finally {
        editor.getDocument().stopGuardedBlockChecking();
      }
      if (tabnineInlayRenderer.hasInlays()) {
        tabnineInlayRenderer.register(this);
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

    if (tabnineInlayRenderer.hasInlays()) {
      tabnineInlayRenderer.clear();
      editor.removeEditorMouseMotionListener(this);
    }

    editor.getContentComponent().removeKeyListener(previewKeyListener);
    editor.getCaretModel().removeCaretListener(caretMoveListener);

    completions = null;
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

  @Override
  public void dispose() {
    clear();
    editor.putUserData(INLINE_COMPLETION_PREVIEW, null);
    CompletionState.clearCompletionState(editor);
  }

  @Override
  public void mouseMoved(@NotNull EditorMouseEvent e) {
    alarm.cancelAllRequests();
    if (!tabnineInlayRenderer.hasInlays()) {
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
    GenericInlay inline = tabnineInlayRenderer.getInline();
    try {
      Rectangle bounds = inline.inner().getBounds();
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
    int inlayOffset = inline.inner().getOffset();
    return pointOffset >= inlayOffset && pointOffset <= inlayOffset + suffix.length();
  }

  @Override
  public void mouseDragged(@NotNull EditorMouseEvent e) {}

  @Nullable
  public Integer getStartOffset() {
    return ObjectUtils.doIfNotNull(tabnineInlayRenderer.getInline().inner(), Inlay::getOffset);
  }

  void applyPreview() {
    inApplyMode.set(true);
    TabnineDocumentListener.mute();
    GenericInlay inline = tabnineInlayRenderer.getInline();

    try {
      int startOffset = inline.inner().getOffset() - completions.get(previewIndex).completionPrefix.length();
      int endOffset = inline.inner().getOffset() + suffix.length();
      editor.getDocument().insertString(inline.inner().getOffset(), suffix);
      editor.getCaretModel().moveToOffset(endOffset);
      AutoImporter.registerTabNineAutoImporter(editor, file.getProject(), startOffset, endOffset);
      previewListener.previewSelected(
          new CompletionPreviewListener.CompletionPreviewData(completions, previewIndex, file));
      inApplyMode.set(false);
      Disposer.dispose(CompletionPreview.this);
    } catch (Throwable e) {
      Logger.getInstance(getClass()).warn("Error on committing the inline completion", e);
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
        if (!CompletionPreview.this.tabnineInlayRenderer.hasInlays()) {
          return;
        }
        if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE
            || event.getKeyCode() == KeyEvent.VK_DELETE) {
          Disposer.dispose(CompletionPreview.this);
        }
      } catch (Throwable err) {
        Logger.getInstance(getClass()).warn("Error in Tabnine preview KeyListener", err);
      }
    }
  }
}
