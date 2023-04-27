package com.tabnineCommon.selections;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.ShowIntentionsPass;
import com.intellij.codeInsight.intention.impl.ShowIntentionActionsHandler;
import com.intellij.openapi.Disposable;
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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoImporter implements MarkupModelListener {

  private static final int MAX_ATTEMPTS = 2;
  private static final Key<AutoImporter> TABNINE_AUTO_IMPORTER_KEY =
      Key.create("TABNINE_AUTO_IMPORTER");

  private final int startOffset;
  private final int endOffset;
  private boolean offsetRangeVisited = false;
  private final AtomicBoolean codeAnalyzerAutoImportInvoked = new AtomicBoolean(false);
  @NotNull private final Project project;
  @NotNull Editor editor;
  private Disposable myDisposable;
  private final Map<String, Integer> importCandidates = new HashMap<>();
  private String lastCandidate;
  private final Set<String> foundImportsSet = new HashSet<>();
  private final List<HighlightInfo.IntentionActionDescriptor> importFixes = new ArrayList<>();

  private AutoImporter(
      @NotNull Editor editor, @NotNull Project project, int startOffset, int endOffset) {
    this.editor = editor;
    this.project = project;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
  }

  private void init() {
    String insertedText = editor.getDocument().getText(new TextRange(startOffset, endOffset));
    String[] terms = insertedText.split("\\W+");
    if (terms.length > 0) {
      for (String term : terms) {
        importCandidates.put(term, MAX_ATTEMPTS);
      }
      lastCandidate = terms[terms.length - 1];
      myDisposable = Disposer.newDisposable();
      EditorUtil.disposeWithEditor(editor, myDisposable);
      ((EditorEx) editor)
          .getFilteredDocumentMarkupModel()
          .addMarkupModelListener(myDisposable, this);
    }
  }

  public static void registerTabNineAutoImporter(
      @NotNull Editor editor, @NotNull Project project, int startOffset, int endOffset) {
    AutoImporter autoImporter = editor.getUserData(TABNINE_AUTO_IMPORTER_KEY);
    if (autoImporter != null) {
      autoImporter.cleanup();
      editor.putUserData(TABNINE_AUTO_IMPORTER_KEY, null);
    }
    autoImporter = new AutoImporter(editor, project, startOffset, endOffset);
    editor.putUserData(TABNINE_AUTO_IMPORTER_KEY, autoImporter);
    autoImporter.init();
  }

  private boolean isPriorHighlighter(@NotNull RangeHighlighterEx highlighter) {
    return startOffset > highlighter.getAffectedAreaStartOffset();
  }

  private boolean isPosteriorHighlighter(@NotNull RangeHighlighterEx highlighter) {
    return endOffset < highlighter.getAffectedAreaEndOffset();
  }

  @Nullable
  private PsiFile getFile(@NotNull RangeHighlighterEx highlighter) {
    final Document document = highlighter.getDocument();
    return PsiDocumentManager.getInstance(project).getPsiFile(document);
  }

  @Nullable
  private HighlightInfo getHighlightInfo(@NotNull RangeHighlighterEx highlighter) {
    Object errorTooltip = highlighter.getErrorStripeTooltip();
    return ObjectUtils.tryCast(errorTooltip, HighlightInfo.class);
  }

  private void invokeLater(@NotNull Runnable task) {
    ServiceManager.invokeLater(task);
  }

  private void autoImportUsingCodeAnalyzer(@NotNull final PsiFile file) {
    if (codeAnalyzerAutoImportInvoked.get()) {
      return;
    }
    final DaemonCodeAnalyzerImpl codeAnalyzer =
        (DaemonCodeAnalyzerImpl) DaemonCodeAnalyzer.getInstance(project);
    CommandProcessor.getInstance()
        .runUndoTransparentAction(
            () ->
                invokeLater(
                    () -> {
                      codeAnalyzerAutoImportInvoked.set(true);
                      codeAnalyzer.autoImportReferenceAtCursor(editor, file);
                    }));
  }

  private void collectImportFixes(
      @NotNull PsiFile file,
      @NotNull RangeHighlighterEx highlighter,
      @NotNull String highlightedText) {
    List<HighlightInfo.IntentionActionDescriptor> availableFixes =
        ShowIntentionsPass.getAvailableFixes(editor, file, -1, highlighter.getEndOffset());
    List<HighlightInfo.IntentionActionDescriptor> importActions =
        availableFixes.stream()
            .filter(
                f -> {
                  String actionText = f.getAction().getText().toLowerCase();
                  return actionText.contains("import")
                      && !(actionText.contains("remove")
                          || actionText.contains("optimize")
                          || actionText.contains("install"));
                })
            .collect(Collectors.toList());
    if (importActions.stream()
            .filter(f -> f.getAction().getText().contains(highlightedText))
            .count()
        > 1) {
      // Skip highlight in case of ambiguity
      return;
    }
    if (!importActions.isEmpty()) {
      foundImportsSet.add(highlightedText);
      importCandidates.remove(highlightedText);
      importFixes.add(importActions.get(0));
    }
  }

  @Override
  public void afterAdded(@NotNull RangeHighlighterEx highlighter) {
    try {
      if (isPriorHighlighter(highlighter)) {
        // Irrelevant
        return;
      }
      PsiFile file = getFile(highlighter);
      if (file == null) {
        return;
      }
      if ((isPosteriorHighlighter(highlighter) && offsetRangeVisited)
          || importCandidates.isEmpty()) {
        // Moved on to other highlighters - invoke the collected actions (if any)
        invokeImportActions(file);
        return;
      }
      offsetRangeVisited = true;
      HighlightInfo highlightInfo = getHighlightInfo(highlighter);
      if (highlightInfo == null) {
        return;
      }
      String highlightedText = highlightInfo.getText();
      if (!importCandidates.containsKey(highlightedText)) {
        // Irrelevant
        return;
      }
      // Try auto import
      autoImportUsingCodeAnalyzer(file);
      // Collect import fixes
      collectImportFixes(file, highlighter, highlightedText);
      markAsVisited(highlightedText);
      if (shouldFinishListening(highlightedText)) {
        // Invoke actions and cleanup
        invokeImportActions(file);
      }
    } catch (Throwable e) {
      Logger.getInstance(getClass())
          .warn("Failed to process highlighter: " + highlighter + " for auto-import", e);
    }
  }

  private boolean shouldFinishListening(@NotNull String term) {
    return (term.equals(lastCandidate) && foundImportsSet.contains(term))
        || importCandidates.isEmpty();
  }

  private void markAsVisited(@NotNull String term) {
    importCandidates.computeIfPresent(term, (k, v) -> v == 1 ? null : v - 1);
  }

  private void invokeImportActions(@NotNull PsiFile file) {
    try {
      final List<HighlightInfo.IntentionActionDescriptor> importFixActions =
          new ArrayList<>(importFixes);
      invokeLater(
          () ->
              importFixActions.forEach(
                  fix ->
                      ShowIntentionActionsHandler.chooseActionAndInvoke(
                          file, editor, fix.getAction(), fix.getAction().getText())));
    } catch (Throwable e) {
      Logger.getInstance(getClass()).warn("Failed to auto import", e);
    } finally {
      cleanup();
    }
  }

  private void cleanup() {
    unregisteredAsMarkupModelListener();
    importCandidates.clear();
    lastCandidate = null;
    foundImportsSet.clear();
    importFixes.clear();
    offsetRangeVisited = false;
    codeAnalyzerAutoImportInvoked.set(false);
  }

  private void unregisteredAsMarkupModelListener() {
    if (myDisposable != null && !Disposer.isDisposed(myDisposable)) {
      Disposer.dispose(myDisposable);
    }
  }
}
