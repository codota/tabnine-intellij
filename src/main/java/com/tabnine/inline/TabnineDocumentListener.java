package com.tabnine.inline;

import com.intellij.codeInsight.completion.CompletionData;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.ObjectUtils;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.ResultEntry;
import com.tabnine.general.DependencyContainer;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.prediction.TabNinePrefixMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.tabnine.general.StaticConfig.MAX_COMPLETIONS;
import static com.tabnine.general.Utils.endsWithADot;

public class TabnineDocumentListener implements DocumentListener {
    private final CompletionFacade completionFacade = DependencyContainer.instanceOfCompletionFacade();

    private CompletionPreview preview;
    private String lastDisplayedInline;

    private static final AtomicBoolean isMuted = new AtomicBoolean(false);

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        System.out.println("--> documentChanged with offset=" + event.getOffset() + ", newFragment=" + event.getNewFragment());
        if (isMuted.get() || ToggleCompletionModeAction.shouldUseCompletionMenu()) {
            return;
        }
        Document document = event.getDocument();
        if (ObjectUtils.doIfCast(document, DocumentEx.class, DocumentEx::isInBulkUpdate) == Boolean.TRUE) {
            return;
        }
        Editor editor = getActiveEditor(document);
        Project project = ObjectUtils.doIfNotNull(editor, Editor::getProject);
        PsiFile file = ObjectUtils.doIfNotNull(project, proj -> PsiDocumentManager.getInstance(proj).getPsiFile(document));
        if (editor == null || project == null || file == null) {
            return;
        }
        if (preview == null) {
            preview = CompletionPreview.findOrCreateCompletionPreview(editor);
        }

//        buildInlineCompletionParameters(event).ifPresent(p -> System.out.println("prefix=" + p.getPrefix() + ", offset=" + p.getOffset()));
//
        InlineCompletionHandler handler = new InlineCompletionHandler(true);
        handler.invoke(project, editor, file, event.getOffset() + event.getNewLength());

//        if (event.getNewFragment().toString().equals(lastDisplayedInline)) {
//            return;
//        }
//        preview.clear();
//        if (event.getNewFragment().length() == 0) {
//            return;
//        }
//        buildInlineCompletionParameters(event).ifPresent(this::showTabninePreviewCompletion);
    }

    public static void mute() {
        isMuted.set(true);
    }

    public static void unmute() {
        isMuted.set(false);
    }

    private void showTabninePreviewCompletion(@NotNull InlineCompletionParameters parameters) {
        Editor editor = parameters.getEditor();
        ReadAction.nonBlocking(
                () -> {
                    AutocompleteResponse completionsResponse =
                            this.completionFacade.retrieveCompletions(parameters);

                    if (completionsResponse == null
                            || completionsResponse.results.length == 0) {
                        return null;
                    }
                    return createCompletions(completionsResponse, parameters);
                })
                .expireWhen(editor::isDisposed)
                .finishOnUiThread(
                        ModalityState.NON_MODAL,
                        completions -> {
                            if (completions == null) {
                                System.out.println("--> Got null instead of completions");
                                return;
                            }
                            System.out.println("--> Got the following completions:");
                            System.out.println(
                                    completions.stream()
                                            .map(x -> x.newPrefix + x.newSuffix)
                                            .collect(Collectors.joining(",\n", "[", "]")));
                            lastDisplayedInline = preview.updatePreview(completions.get(0), parameters.getOffset());
                        })
                .submit(AppExecutorUtil.getAppExecutorService());
    }

    private ArrayList<TabNineCompletion> createCompletions(AutocompleteResponse completions, @NotNull InlineCompletionParameters parameters) {
        ArrayList<TabNineCompletion> result = new ArrayList<>();
        PrefixMatcher prefixMatcher = new TabNinePrefixMatcher(new PlainPrefixMatcher(parameters.getPrefix()));
        for (int index = 0; index < completions.results.length && index < completionLimit(parameters, completions.is_locked); index++) {
            TabNineCompletion completion = createCompletion(
                    parameters, completions.old_prefix,
                    completions.results[index], index, completions.is_locked);

            if (prefixMatcher.prefixMatches(completion.newPrefix)) {
                result.add(completion);
            }
        }

        return result;
    }

    private int completionLimit(@NotNull InlineCompletionParameters parameters, boolean isLocked) {
        if (isLocked) {
            return 1;
        }
        boolean preferTabNine = !endsWithADot(
                parameters.getDocument(),
                parameters.getOffset() - parameters.getNewFragment().length()
        );

        return preferTabNine ? MAX_COMPLETIONS : 1;
    }

    @NotNull
    private TabNineCompletion createCompletion(@NotNull InlineCompletionParameters parameters,
                                           String oldPrefix, ResultEntry result, int index, boolean isLocked) {
        TabNineCompletion completion = new TabNineCompletion(
                oldPrefix,
                result.new_prefix,
                result.old_suffix,
                result.new_suffix,
                index,
                parameters.getNewFragment().toString(),
                getCursorPrefix(parameters),
                getCursorSuffix(parameters),
                result.origin
        );

        completion.detail = result.detail;

        if (result.deprecated != null) {
            completion.deprecated = result.deprecated;
        }
        return completion;
    }

    private String getCursorPrefix(@NotNull InlineCompletionParameters parameters) {
        Document document = parameters.getDocument();
        int cursorPosition = parameters.getOffset();
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineStart = document.getLineStartOffset(lineNumber);

        return document.getText(TextRange.create(lineStart, cursorPosition)).trim();
    }

    private String getCursorSuffix(@NotNull InlineCompletionParameters parameters) {
        Document document = parameters.getDocument();
        int cursorPosition = parameters.getOffset();
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineEnd = document.getLineEndOffset(lineNumber);

        return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    }

    @Nullable
    private static Editor getActiveEditor(@NotNull Document document) {
        Component focusOwner = IdeFocusManager.getGlobalInstance().getFocusOwner();
        DataContext dataContext = DataManager.getInstance().getDataContext(focusOwner);
        // ignore caret placing when exiting
        Editor activeEditor = ApplicationManager.getApplication().isDisposed() ? null : CommonDataKeys.EDITOR.getData(dataContext);
        if (activeEditor != null && activeEditor.getDocument() != document) {
            activeEditor = null;
        }
        return activeEditor;
    }

    private Optional<InlineCompletionParameters> buildInlineCompletionParameters(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        Editor editor = getActiveEditor(document);
        Project project = ObjectUtils.doIfNotNull(editor, Editor::getProject);
        PsiFile file = ObjectUtils.doIfNotNull(project, proj -> PsiDocumentManager.getInstance(proj).getPsiFile(document));
        if (editor == null || project == null || file == null) {
            return Optional.empty();
        }
        int newOffset = event.getOffset() + event.getNewFragment().length();
        String documentText = document.getText();
        if (newOffset < 0 || newOffset > documentText.length() - 1) {
            return Optional.empty();
        }
        documentText = new StringBuilder(documentText).insert(newOffset, "TabnineDummy").toString();
        file = PsiFileFactory.getInstance(project).createFileFromText("tmp-" + file.getName(), file.getFileType(), documentText);
        Document copyDocument = file.getViewProvider().getDocument();
        if (copyDocument == null) {
            return Optional.empty();
        }
//        PsiDocumentManager.getInstance(project).commitDocument(document);
        PsiElement element = file.findElementAt(event.getOffset());
        if (element == null) {
            return Optional.empty();
        }
        System.out.println("--> current element is " + element);
        String prefix = CompletionData.findPrefixStatic(element, event.getOffset());
        System.out.println("--> prefix is " + prefix);
    return Optional.of(
        new InlineCompletionParameters(
            editor,
            project,
            document,
            prefix,
            event.getNewFragment(),
            event.getOffset() + event.getNewLength()));
    }
}
