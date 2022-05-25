package com.tabnine.inline;

import static com.tabnine.inline.CompletionPreviewUtilsKt.hadSuffix;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

public class CompletionPreview implements Disposable {
  private static final String NO_SUFFIX = "";
  private static final Key<CompletionPreview> INLINE_COMPLETION_PREVIEW =
      Key.create("INLINE_COMPLETION_PREVIEW");
  public static final @Nullable String NO_SHOWING_PREVIEW = null;

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
  private final AtomicBoolean wasCleared = new AtomicBoolean(false);

  private CompletionPreview(@NotNull Editor editor, @NotNull PsiFile file) {
    this.tabnineInlay = TabnineInlay.create(this);
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

  public static void clear(@NotNull Editor editor) {
    CompletionPreview completionPreview = getInstance(editor);
    if (completionPreview != null) {
      completionPreview.clear();
    }
  }

  @TestOnly
  public static String getPreviewText(@NotNull Editor editor) {
    CompletionPreview preview = getInstance(editor);

    if (preview == null || preview.completions == null) {
      return null;
    }

    return preview.completions.get(preview.previewIndex).getSuffix();
  }

  public void willUpdatePreview() {
    wasCleared.set(false);
  }

  @Nullable
  public String updatePreview(
      @NotNull List<TabNineCompletion> completions, int previewIndex, int offset) {
    if (SuggestionsMode.getSuggestionMode() != SuggestionsMode.INLINE) {
      return NO_SHOWING_PREVIEW;
    }

    if (wasCleared.get()) {
      return NO_SHOWING_PREVIEW;
    }

    TabNineCompletion completion = completions.get(previewIndex);
    String suffix = completion.getSuffix();

    this.completions = completions;
    this.previewIndex = previewIndex;
    this.tabnineInlay.clear();

    if (suffix.isEmpty()
        || !(editor instanceof EditorImpl)
        || editor.getSelectionModel().hasSelection()
        || InplaceRefactoring.getActiveInplaceRenamer(editor) != null) {
      return NO_SHOWING_PREVIEW;
    }

    insertionHint.updateSuffix(suffix);

    try {
      editor.getDocument().startGuardedBlockChecking();
      tabnineInlay.render(this.editor, completion, offset);
      editor.getContentComponent().addKeyListener(keyListener);
    } finally {
      editor.getDocument().stopGuardedBlockChecking();
    }

    return suffix;
  }

  public void clear() {
    if (inApplyMode.get()) {
      return;
    }

    wasCleared.set(true);
    tabnineInlay.clear();
    editor.getContentComponent().removeKeyListener(keyListener);
    insertionHint.updateSuffix(NO_SUFFIX);

    completions = null;
  }

  public boolean shouldAcceptSuggestion(Editor editor, Caret caret) {
    Integer offset = tabnineInlay.getOffset();

    if (tabnineInlay.isEmpty() || offset == null) {
      return false;
    }

    return Objects.equals(
        editor.offsetToLogicalPosition(caret.getOffset()).line,
        editor.offsetToLogicalPosition(offset).line);
  }

  public void applyPreview(@Nullable Caret caret) {
    inApplyMode.set(true);
    TabnineDocumentListener.mute();

    if (caret == null) {
      return;
    }

    try {
      applyPreviewInternal(caret.getOffset());
    } catch (Throwable e) {
      Logger.getInstance(getClass()).error("Failed in the processes of accepting completion", e);
    } finally {
      inApplyMode.set(false);
      clear();
      TabnineDocumentListener.unmute();
    }
  }

  private void applyPreviewInternal(@NotNull Integer cursorOffset) {
    TabNineCompletion completion = completions.get(previewIndex);
    String suffix = completion.getSuffix();
    int startOffset = cursorOffset - completion.oldPrefix.length();
    int endOffset = cursorOffset + suffix.length();

    if (hadSuffix(completion)) {
      editor.getDocument().deleteString(cursorOffset, cursorOffset + completion.oldSuffix.length());
    }

    editor.getDocument().insertString(cursorOffset, suffix);
    editor.getCaretModel().moveToOffset(startOffset + completion.newPrefix.length());
    AutoImporter.registerTabNineAutoImporter(editor, file.getProject(), startOffset, endOffset);
    previewListener.previewSelected(
        new CompletionPreviewListener.CompletionPreviewData(completions, previewIndex, file));
  }

  @Override
  public void dispose() {
    clear();
    editor.putUserData(INLINE_COMPLETION_PREVIEW, null);
    CompletionState.clearCompletionState(editor);
  }
}
