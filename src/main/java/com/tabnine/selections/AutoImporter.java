package com.tabnine.selections;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.ShowIntentionsPass;
import com.intellij.codeInsight.intention.impl.ShowIntentionActionsHandler;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.event.MarkupModelListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoImporter implements MarkupModelListener {

  private static final Key<AutoImporter> TABNINE_AUTO_IMPORTER_KEY =
      Key.create("TABNINE_AUTO_IMPORTER");

  private int startOffset;
  private int endOffset;
  @NotNull private final Project project;
  @NotNull Editor editor;
  private Disposable myDisposable;
  private final Map<String, Pair<Integer, Boolean>> possibleRequiredImports = new HashMap<>();
  private final List<HighlightInfo.IntentionActionDescriptor> importFixes = new ArrayList<>();

  private AutoImporter(@NotNull InsertionContext context) {
    project = context.getProject();
    editor = context.getEditor();
  }

  private void init(@NotNull InsertionContext context) {
    myDisposable = Disposer.newDisposable();
    EditorUtil.disposeWithEditor(editor, myDisposable);
    ((EditorEx) editor).getFilteredDocumentMarkupModel().addMarkupModelListener(myDisposable, this);

    startOffset = context.getStartOffset();
    endOffset = context.getTailOffset();
    String insertedText = context.getDocument().getText(new TextRange(startOffset, endOffset));
    int index = 0;
    for (String term : insertedText.split("\\W+")) {
      if (possibleRequiredImports.containsKey(term)) {
        continue;
      }
      possibleRequiredImports.put(term, Pair.create(++index, false));
    }
  }

  public static void registerTabNineAutoImporter(
      @NotNull InsertionContext context) {
    Editor editor = context.getEditor();
    AutoImporter autoImporter = editor.getUserData(TABNINE_AUTO_IMPORTER_KEY);
    if (autoImporter != null) {
      autoImporter.cleanup();
      editor.putUserData(TABNINE_AUTO_IMPORTER_KEY, null);
    }
    autoImporter = new AutoImporter(context);
    editor.putUserData(TABNINE_AUTO_IMPORTER_KEY, autoImporter);
    autoImporter.init(context);
  }

  @Override
  public void afterAdded(@NotNull RangeHighlighterEx highlighter) {
    if (startOffset > highlighter.getAffectedAreaStartOffset()) {
      return;
    }
    final Document document = highlighter.getDocument();
    final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
    if (file == null) {
      return;
    }
    if (endOffset < highlighter.getAffectedAreaEndOffset()) {
      invokeImportActions(file);
      return;
    }
    Object errorTooltip = highlighter.getErrorStripeTooltip();
    HighlightInfo highlightInfo = ObjectUtils.tryCast(errorTooltip, HighlightInfo.class);
    if (highlightInfo == null) {
      return;
    }
    String highlightedText = highlightInfo.getText();
    if (!isRelevant(highlightedText)) {
      if (isLastToVisit(highlightedText)) {
        // Last one - invoke actions and cleanup
        invokeImportActions(file);
      }
      return;
    }
    // Try auto import
    final DaemonCodeAnalyzerImpl codeAnalyzer =
        (DaemonCodeAnalyzerImpl) DaemonCodeAnalyzer.getInstance(project);
    CommandProcessor.getInstance()
        .runUndoTransparentAction(() -> codeAnalyzer.autoImportReferenceAtCursor(editor, file));
    // Collect import fixes
    List<HighlightInfo.IntentionActionDescriptor> availableFixes =
        ShowIntentionsPass.getAvailableFixes(editor, file, -1, highlighter.getEndOffset());
    availableFixes.stream()
        .filter(f -> f.getAction().getText().toLowerCase().contains("import"))
        .findFirst()
        .ifPresent(importFixes::add);
    markAsVisited(highlightedText);
    if (isLastToVisit(highlightedText)) {
      // Last one - invoke actions and cleanup
      invokeImportActions(file);
    }
  }

  private boolean isRelevant(String term) {
    Pair<Integer, Boolean> pair = possibleRequiredImports.get(term);
    if (pair == null) {
      return false;
    }
    return !pair.getSecond();
  }

  private boolean isLastToVisit(String term) {
    Pair<Integer, Boolean> pair = possibleRequiredImports.get(term);
    return pair != null && pair.getFirst() == possibleRequiredImports.size();
  }

  private void markAsVisited(String term) {
    possibleRequiredImports.computeIfPresent(term, (k, v) -> Pair.create(v.getFirst(), true));
  }

  private void invokeImportActions(@NotNull PsiFile file) {
    try {
      WriteAction.run(() -> {
        importFixes.forEach(
                fix -> ShowIntentionActionsHandler.chooseActionAndInvoke(
                        file, editor, fix.getAction(), fix.getAction().getText()));
      });
    } catch (Throwable e) {
      Logger.getInstance(getClass()).warn("Failed to auto import", e);
    } finally {
      cleanup();
    }
  }

  private void cleanup() {
    unregisteredAsMarkupModelListener();
    possibleRequiredImports.clear();
    importFixes.clear();
  }

  private void unregisteredAsMarkupModelListener() {
    if (!Disposer.isDisposed(myDisposable)) {
      Disposer.dispose(myDisposable);
    }
  }
}
