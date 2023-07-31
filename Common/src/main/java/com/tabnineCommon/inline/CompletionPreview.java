package com.tabnineCommon.inline;

import static com.tabnineCommon.inline.CompletionPreviewUtilsKt.*;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;
import com.tabnineCommon.capabilities.RenderingMode;
import com.tabnineCommon.general.CompletionsEventSender;
import com.tabnineCommon.general.DependencyContainer;
import com.tabnineCommon.inline.listeners.InlineCaretListener;
import com.tabnineCommon.inline.listeners.InlineFocusListener;
import com.tabnineCommon.inline.render.TabnineInlay;
import com.tabnineCommon.prediction.TabNineCompletion;
import com.tabnineCommon.selections.CompletionPreviewListener;
import com.tabnineCommon.selections.SelectionUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompletionPreview implements Disposable {
  private static final Key<CompletionPreview> INLINE_COMPLETION_PREVIEW =
      Key.create("INLINE_COMPLETION_PREVIEW");

  private final CompletionPreviewListener previewListener =
      DependencyContainer.instanceOfCompletionPreviewListener();
  private final CompletionsEventSender completionsEventSender =
      DependencyContainer.instanceOfCompletionsEventSender();

  public final Editor editor;
  private TabnineInlay tabnineInlay;
  private List<TabNineCompletion> completions;
  private final int offset;
  private int currentIndex = 0;

  private final InlineCaretListener caretListener;
  private final InlineFocusListener focusListener;

  private CompletionPreview(
      @NotNull Editor editor, List<TabNineCompletion> completions, int offset) {
    this.editor = editor;
    this.completions = completions;
    this.offset = offset;
    EditorUtil.disposeWithEditor(editor, this);

    tabnineInlay = TabnineInlay.create(this);
    caretListener = new InlineCaretListener(this);
    focusListener = new InlineFocusListener(this);
  }

  public static TabNineCompletion createInstance(
      Editor editor, List<TabNineCompletion> completions, int offset) {
    CompletionPreview preview = getInstance(editor);

    if (preview != null) {
      Disposer.dispose(preview);
    }

    preview = new CompletionPreview(editor, completions, offset);

    editor.putUserData(INLINE_COMPLETION_PREVIEW, preview);

    return preview.createPreview();
  }

  @Nullable
  public static TabNineCompletion getCurrentCompletion(Editor editor) {
    CompletionPreview preview = getInstance(editor);
    if (preview == null) return null;

    return preview.getCurrentCompletion();
  }

  @Nullable
  public static CompletionPreview getInstance(@NotNull Editor editor) {
    return editor.getUserData(INLINE_COMPLETION_PREVIEW);
  }

  public static void clear(@NotNull Editor editor) {
    CompletionPreview completionPreview = getInstance(editor);
    if (completionPreview != null) {
      Disposer.dispose(completionPreview);
    }
  }

  public void togglePreview(CompletionOrder order) {
    int nextIndex = currentIndex + order.diff();
    currentIndex = (completions.size() + nextIndex) % completions.size();

    Disposer.dispose(tabnineInlay);
    tabnineInlay = TabnineInlay.create(this);

    createPreview();
    completionsEventSender.sendToggleInlineSuggestionEvent(order, currentIndex);
  }

  public TabNineCompletion getCurrentCompletion() {
    return completions.get(currentIndex);
  }

  private TabNineCompletion createPreview() {
    TabNineCompletion completion = completions.get(currentIndex);

    if (!(editor instanceof EditorImpl)
        || editor.getSelectionModel().hasSelection()
        || InplaceRefactoring.getActiveInplaceRenamer(editor) != null) {
      return null;
    }

    try {
      editor.getDocument().startGuardedBlockChecking();
      tabnineInlay.render(this.editor, completion, offset);
      return completion;
    } finally {
      editor.getDocument().stopGuardedBlockChecking();
    }
  }

  public void dispose() {
    editor.putUserData(INLINE_COMPLETION_PREVIEW, null);
  }

  public void applyPreview(@Nullable Caret caret) {
    if (caret == null) {
      return;
    }

    Project project = editor.getProject();

    if (project == null) {
      return;
    }

    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

    if (file == null) {
      return;
    }

    try {
      applyPreviewInternal(caret.getOffset(), project, file);
    } catch (Throwable e) {
      Logger.getInstance(getClass()).warn("Failed in the processes of accepting completion", e);
    } finally {
      Disposer.dispose(this);
    }
  }

  private void applyPreviewInternal(@NotNull Integer cursorOffset, Project project, PsiFile file) {
    CompletionPreview.clear(editor);
    TabNineCompletion completion = completions.get(currentIndex);
    String suffix = completion.getSuffix();
    int startOffset = cursorOffset - completion.oldPrefix.length();
    int endOffset = cursorOffset + suffix.length();

    if (shouldRemoveSuffix(completion)) {
      editor.getDocument().deleteString(cursorOffset, cursorOffset + completion.oldSuffix.length());
    }

    editor.getDocument().insertString(cursorOffset, suffix);
    editor.getCaretModel().moveToOffset(startOffset + completion.newPrefix.length());
    //    if (AppSettingsState.getInstance().getAutoImportEnabled()) {
    //      Logger.getInstance(getClass()).info("Registering auto importer");
    //      AutoImporter.registerTabNineAutoImporter(editor, project, startOffset, endOffset);
    //    }
    previewListener.executeSelection(
        this.editor,
        completion,
        file.getName(),
        RenderingMode.INLINE,
        (selection -> {
          selection.index = currentIndex;
          SelectionUtil.addSuggestionsCount(selection, completions);
        }));
  }
}
