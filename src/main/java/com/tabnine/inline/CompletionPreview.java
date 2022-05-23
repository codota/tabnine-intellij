package com.tabnine.inline;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;
import com.intellij.util.ObjectUtils;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.general.DependencyContainer;
import com.tabnine.inline.listeners.InlineCaretListener;
import com.tabnine.inline.listeners.InlineFocusListener;
import com.tabnine.inline.listeners.InlineKeyListener;
import com.tabnine.inline.render.TabnineInlay;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.selections.AutoImporter;
import com.tabnine.selections.CompletionPreviewListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

public class CompletionPreview implements Disposable {
  private static final String NO_SUFFIX = "";
  private static final Key<CompletionPreview> INLINE_COMPLETION_PREVIEW =
      Key.create("INLINE_COMPLETION_PREVIEW");

  private final CompletionPreviewListener previewListener =
      DependencyContainer.instanceOfCompletionPreviewListener();
  public final Editor editor;
  private final PsiFile file;
  private final CompletionPreviewInsertionHint insertionHint;
  private List<TabNineCompletion> completions;
  private int previewIndex;
  private final TabnineInlay tabnineInlay;
  private final InlineKeyListener keyListener;
  private final AtomicBoolean inApplyMode = new AtomicBoolean(false);

  private CompletionPreview(@NotNull Editor editor, @NotNull PsiFile file) {
    this.tabnineInlay = TabnineInlay.create();
    this.editor = editor;
    this.file = file;
    this.keyListener = new InlineKeyListener(editor);
    this.insertionHint = new CompletionPreviewInsertionHint(editor, tabnineInlay, NO_SUFFIX);

    editor.getCaretModel().addCaretListener(new InlineCaretListener());
    ObjectUtils.consumeIfCast(
        editor, EditorEx.class, e -> e.addFocusListener(new InlineFocusListener()));
  }

  @NotNull
  public static CompletionPreview getOrCreateInstance(
      @NotNull Editor editor, @NotNull PsiFile file) {
    CompletionPreview preview = getInstance(editor);

    if (preview == null) {
      preview = new CompletionPreview(editor, file);
      EditorUtil.disposeWithEditor(editor, preview);
      editor.putUserData(INLINE_COMPLETION_PREVIEW, preview);
    }

    return preview;
  }

  @Nullable
  public static CompletionPreview getInstance(@NotNull Editor editor) {
    return editor.getUserData(INLINE_COMPLETION_PREVIEW);
  }

  public static void disposeIfExists(@NotNull Editor editor) {
    CompletionPreview completionPreview = getInstance(editor);
    if (completionPreview != null) {
      Disposer.dispose(completionPreview);
    }
  }

  @TestOnly
  public static String getPreviewText(@NotNull Editor editor) {
    CompletionPreview preview = editor.getUserData(INLINE_COMPLETION_PREVIEW);

    if (preview == null) {
      return null;
    }

    return preview.completions.get(preview.previewIndex).getSuffix();
  }

  public boolean isCurrentlyDisplayingInlays() {
    return !tabnineInlay.isEmpty();
  }

  @Nullable
  public String updatePreview(
      @NotNull List<TabNineCompletion> completions, int previewIndex, int offset) {
    if (SuggestionsMode.getSuggestionMode() != SuggestionsMode.INLINE) {
      return null;
    }

    this.completions = completions;
    this.previewIndex = previewIndex;
    TabNineCompletion completion = completions.get(previewIndex);
    this.tabnineInlay.clear();

    String suffix = completion.getSuffix();
    insertionHint.updateSuffix(suffix);

    if (!suffix.isEmpty()
        && editor instanceof EditorImpl
        && !editor.getSelectionModel().hasSelection()
        && InplaceRefactoring.getActiveInplaceRenamer(editor) == null) {
      editor.getDocument().startGuardedBlockChecking();

      try {
        tabnineInlay.render(this.editor, completion, offset);
      } finally {
        editor.getDocument().stopGuardedBlockChecking();
      }

      if (!tabnineInlay.isEmpty()) {
        tabnineInlay.register(this);
        editor.getContentComponent().addKeyListener(keyListener);
      }
    }

    return suffix;
  }

  public void clear() {
    if (inApplyMode.get()) {
      return;
    }

    if (!tabnineInlay.isEmpty()) {
      tabnineInlay.clear();
    }

    editor.getContentComponent().removeKeyListener(keyListener);

    completions = null;
    insertionHint.updateSuffix(NO_SUFFIX);
  }

  @Override
  public void dispose() {
    clear();
    editor.putUserData(INLINE_COMPLETION_PREVIEW, null);
    CompletionState.clearCompletionState(editor);
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
      int startOffset = renderedOffset - currentCompletion.oldPrefix.length();
      String suffix = currentCompletion.getSuffix();
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
}
