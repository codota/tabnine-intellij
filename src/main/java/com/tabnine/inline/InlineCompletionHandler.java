package com.tabnine.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.completion.CompletionData;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.ResultEntry;
import com.tabnine.general.DependencyContainer;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.prediction.TabNinePrefixMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tabnine.general.StaticConfig.MAX_COMPLETIONS;
import static com.tabnine.general.Utils.endsWithADot;

public class InlineCompletionHandler implements CodeInsightActionHandler {
    private static final String INLINE_DUMMY_IDENTIFIER = "TabnineInlineDummy";

    private final CompletionFacade completionFacade = DependencyContainer.instanceOfCompletionFacade();
    private final boolean myForward;

    InlineCompletionHandler(boolean forward) {
        this.myForward = forward;
    }

    void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, int offset) {
        Document document = editor.getDocument();

        // if we cannot modify this file, return
        if (editor.isViewer() || document.getRangeGuard(offset, offset) != null) {
            document.fireReadOnlyModificationAttempt();
            EditorModificationUtil.checkModificationAllowed(editor);
            return;
        }

        // data about previous completions, or just a new fresh state for the editor
        final CompletionState completionState = CompletionState.findOrCreateCompletionState(editor);
        int lastDisplayedCompletionIndex = completionState.lastDisplayedCompletionIndex;

        boolean noOldSuggestion = lastDisplayedCompletionIndex == -1 || completionState.prefix == null;
        boolean editorLocationHasChanged = completionState.lastStartOffset != offset;
//        boolean editorLocationHasChanged = !completionState.caretOffsets.equals(getCaretOffsets(editor));
        boolean documentChanged = completionState.lastModCount != document.getModificationStamp();

        if (noOldSuggestion || editorLocationHasChanged || documentChanged) {
            // start a new query
            completionState.prefix = computeCurrentPrefix(editor, project, file, offset);
            completionState.lastDisplayedCompletionIndex = -1;
            retrieveAndShowInlineCompletion(editor, completionState, offset);
        } else {
            showInlineCompletion(editor, completionState, completionState.lastStartOffset);
        }
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        int caretOffset = editor.getCaretModel().getOffset();
        invoke(project, editor, file, caretOffset);
    }

    private List<Integer> getCaretOffsets(@NotNull Editor editor) {
        return editor.getCaretModel().getAllCarets().stream().map(Caret::getOffset).collect(Collectors.toList());
    }

    private String computeCurrentPrefix(@NotNull Editor editor, @NotNull Project project, @NotNull PsiFile file, int offset) {
        Document document = editor.getDocument();
        String documentText = document.getText();
        if (offset < 0 || offset > documentText.length()) {
            return "";
        }
        documentText = new StringBuilder(documentText).insert(offset, INLINE_DUMMY_IDENTIFIER).toString();


        file = PsiFileFactory.getInstance(project).createFileFromText("tmp-" + file.getName(), file.getFileType(), documentText);
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return "";
        }
        System.out.println("--> current element is " + element);
        String prefix = CompletionData.findPrefixStatic(element, offset);
        System.out.println("--> prefix is " + prefix);
        return prefix;
    }

    private void showInlineCompletion(@NotNull Editor editor, CompletionState completionState, int startOffset) {
        if (completionState.suggestions == null || completionState.suggestions.isEmpty()) {
            return;
        }
        int nextIndex;
        if (myForward) {
            nextIndex = completionState.lastDisplayedCompletionIndex + 1;
            if (nextIndex >= completionState.suggestions.size()) {
                // cycle over to start
                nextIndex = 0;
            }
        } else {
            nextIndex = completionState.lastDisplayedCompletionIndex - 1;
            if (nextIndex < 0) {
                nextIndex = completionState.suggestions.size() - 1;
            }
        }
        final TabNineCompletion nextSuggestion = completionState.suggestions.get(nextIndex);
        if (nextSuggestion == null) {
            return;
        }
        System.out.println("--> Got the following completions:");
        System.out.println(
                completionState.suggestions.stream()
                        .map(x -> x.newPrefix + x.newSuffix)
                        .collect(Collectors.joining(",\n", "[", "]")));
        CompletionPreview preview = CompletionPreview.findOrCreateCompletionPreview(editor);
        completionState.lastDisplayedPreview = preview.updatePreview(nextSuggestion, startOffset);
        completionState.lastDisplayedCompletionIndex = nextIndex;
        completionState.lastStartOffset = startOffset;
        completionState.lastModCount = editor.getDocument().getModificationStamp();
        completionState.suggestionBrowsed(this.myForward);
        InlineHints.showPreInsertionHint(editor);
//        RangeMarker start = editor.getDocument().createRangeMarker(startOffset, startOffset);
    }

    private void retrieveAndShowInlineCompletion(@NotNull Editor editor, CompletionState completionState, int startOffset) {
        final Document document = editor.getDocument();
        final long lastModified = document.getModificationStamp();
        ReadAction.nonBlocking(
                () -> {
                    AutocompleteResponse completionsResponse =
                            this.completionFacade.retrieveCompletions(document, startOffset);

                    if (completionsResponse == null
                            || completionsResponse.results.length == 0) {
                        return null;
                    }
                    return createCompletions(completionsResponse, document, completionState.prefix, startOffset);
                })
                .expireWhen(() -> {
                    if (editor.isDisposed()) {
                        return true;
                    }
                    if (document.getModificationStamp() != lastModified) {
                        System.out.println("--> document has been modified, going to expire the task");
                        return true;
                    }
                    return false;
                })
                .finishOnUiThread(
                        ModalityState.NON_MODAL,
                        completions -> {
                            completionState.suggestions = completions;
                            completionState.resetStats(editor);
                            showInlineCompletion(editor, completionState, startOffset);
                        })
                .submit(AppExecutorUtil.getAppExecutorService());
    }

    private List<TabNineCompletion> createCompletions(AutocompleteResponse completions, @NotNull Document document, @NotNull String prefix, int offset) {
        List<TabNineCompletion> result = new ArrayList<>();
        PrefixMatcher prefixMatcher = new TabNinePrefixMatcher(new PlainPrefixMatcher(prefix));
        for (int index = 0; index < completions.results.length && index < completionLimit(document, prefix, offset, completions.is_locked); index++) {
            TabNineCompletion completion = createCompletion(
                    document, prefix, offset, completions.old_prefix,
                    completions.results[index], index, completions.is_locked);

            if (prefixMatcher.prefixMatches(completion.newPrefix)) {
                result.add(completion);
            }
        }

        return result;
    }

    private int completionLimit(@NotNull Document document, @NotNull String prefix, int offset, boolean isLocked) {
        if (isLocked) {
            return 1;
        }
        boolean preferTabNine = !endsWithADot(
                document,
                offset - prefix.length()
        );

        return preferTabNine ? MAX_COMPLETIONS : 1;
    }

    @NotNull
    private TabNineCompletion createCompletion(@NotNull Document document, String newPrefix, int offset,
                                               String oldPrefix, ResultEntry result, int index, boolean isLocked) {
        TabNineCompletion completion = new TabNineCompletion(
                oldPrefix,
                result.new_prefix,
                result.old_suffix,
                result.new_suffix,
                index,
                newPrefix,
                getCursorPrefix(document, offset),
                getCursorSuffix(document, offset),
                result.origin
        );

        completion.detail = result.detail;

        if (result.deprecated != null) {
            completion.deprecated = result.deprecated;
        }
        return completion;
    }

    private String getCursorPrefix(@NotNull Document document, int cursorPosition) {
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineStart = document.getLineStartOffset(lineNumber);

        return document.getText(TextRange.create(lineStart, cursorPosition)).trim();
    }

    private String getCursorSuffix(@NotNull Document document, int cursorPosition) {
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineEnd = document.getLineEndOffset(lineNumber);

        return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    }
}
