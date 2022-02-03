package com.tabnine.inline;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import com.tabnine.capabilities.SuggestionsMode;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabnineDocumentListener implements DocumentListener {
  public static final int MINIMAL_DELAY_MILLIS = 25;
  private final InlineCompletionHandler handler = new InlineCompletionHandler(true);
  private final Alarm alarm = new Alarm();

  private static final java.util.List<String> AUTO_FILLING_PAIRS =
      Arrays.asList("()", "{}", "[]", "''", "\"\"", "``");
  private static final AtomicBoolean isMuted = new AtomicBoolean(false);

  @Override
  public void documentChanged(@NotNull DocumentEvent event) {
    String eventNewText = event.getNewFragment().toString();
    if (isMuted.get()
        || SuggestionsMode.getSuggestionMode() != SuggestionsMode.INLINE
        || eventNewText.equals(CompletionUtil.DUMMY_IDENTIFIER)
        || event.getNewLength() < 1) {
      return;
    }

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      documentChangedDebounced(event, eventNewText);
    } else {
      alarm.cancelAllRequests();
      // Give enough time to cancel previous requests in cases where the document listener is called
      // too often
      // (e.g. on newline+indents, auto-filling pairs etc.).
      alarm.addRequest(() -> documentChangedDebounced(event, eventNewText), MINIMAL_DELAY_MILLIS);
    }
  }

  private void documentChangedDebounced(@NotNull DocumentEvent event, String eventNewText) {
    Document document = event.getDocument();

    if (ObjectUtils.doIfCast(document, DocumentEx.class, DocumentEx::isInBulkUpdate)
        == Boolean.TRUE) {
      return;
    }
    Editor editor = getActiveEditor(document);

    if (editor != null
        && !editor.getEditorKind().equals(EditorKind.MAIN_EDITOR)
        && !ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }

    Project project = ObjectUtils.doIfNotNull(editor, Editor::getProject);
    PsiFile file =
        ObjectUtils.doIfNotNull(
            project, proj -> PsiDocumentManager.getInstance(proj).getPsiFile(document));
    if (editor == null || project == null || file == null) {
      return;
    }

    int startOffset = event.getOffset();
    int endOffset = event.getOffset() + event.getNewLength();

    if (newTextIsAutoFilled(eventNewText, document, startOffset, endOffset)
        || !newTextIsSingleChange(eventNewText)) {
      return;
    }

    handler.invoke(editor, file, endOffset);
  }

  // counts `\n\t` as a single change too.
  private boolean newTextIsSingleChange(String newText) {
    return newText.length() == 1 || newText.trim().isEmpty();
  }

  private boolean newTextIsAutoFilled(
      String eventNewText, Document document, int startOffset, int endOffset) {
    try {
      String textIncludingPreviousChar =
          document.getText(new TextRange(startOffset - 1, endOffset));

      return AUTO_FILLING_PAIRS.contains(textIncludingPreviousChar)
          || AUTO_FILLING_PAIRS.contains(eventNewText);
    } catch (Throwable e) {
      Logger.getInstance(getClass())
          .debug("Could not determine if document change is auto filled, skipping: ", e);
    }

    return false;
  }

  public static void mute() {
    isMuted.set(true);
  }

  public static void unmute() {
    isMuted.set(false);
  }

  @Nullable
  private static Editor getActiveEditor(@NotNull Document document) {
    if (!ApplicationManager.getApplication().isDispatchThread()) {
      return null;
    }
    Component focusOwner = IdeFocusManager.getGlobalInstance().getFocusOwner();
    DataContext dataContext = DataManager.getInstance().getDataContext(focusOwner);
    // ignore caret placing when exiting
    Editor activeEditor =
        ApplicationManager.getApplication().isDisposed()
            ? null
            : CommonDataKeys.EDITOR.getData(dataContext);
    if (activeEditor != null && activeEditor.getDocument() != document) {
      activeEditor = null;
    }
    return activeEditor;
  }
}
