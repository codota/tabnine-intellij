package com.tabnine.intellij.completions;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.ShowIntentionsPass;
import com.intellij.codeInsight.intention.impl.ShowIntentionActionsHandler;
import com.intellij.codeInsight.lookup.*;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.CaretModelImpl;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.event.MarkupModelListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.messages.MessageBus;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.ResultEntry;
import com.tabnine.config.Config;
import com.tabnine.general.DependencyContainer;
import com.tabnine.general.StaticConfig;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.prediction.TabNinePrefixMatcher;
import com.tabnine.prediction.TabNineWeigher;
import com.tabnine.selections.AutoImporter;
import com.tabnine.selections.TabNineLookupListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.tabnine.general.StaticConfig.*;
import static com.tabnine.general.Utils.endsWithADot;

public class TabNineCompletionContributor extends CompletionContributor {
    private final CompletionFacade completionFacade = DependencyContainer.instanceOfCompletionFacade();
    private final TabNineLookupListener tabNineLookupListener = DependencyContainer.instanceOfTabNineLookupListener();
    private final MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
    private boolean isLocked;

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet) {
        registerLookupListener(parameters);
        AutocompleteResponse completions = this.completionFacade.retrieveCompletions(parameters);

        if (completions == null) {
            return;
        }

        PrefixMatcher originalMatcher = resultSet.getPrefixMatcher();

        if (originalMatcher.getPrefix().length() == 0 && completions.results.length == 0) {
            return;
        }
        if (this.isLocked != completions.is_locked) {
            this.isLocked = completions.is_locked;
            this.messageBus
              .syncPublisher(LimitedSecletionsChangedNotifier.LIMITED_SELECTIONS_CHANGED_TOPIC)
              .limitedChanged(completions.is_locked);
        }

        resultSet = resultSet.withPrefixMatcher(new TabNinePrefixMatcher(originalMatcher.cloneWithPrefix(completions.old_prefix)))
                .withRelevanceSorter(CompletionSorter.defaultSorter(parameters, originalMatcher).weigh(new TabNineWeigher()));
        resultSet.restartCompletionOnAnyPrefixChange();

        addAdvertisement(resultSet, completions);

        resultSet.addAllElements(createCompletions(completions, parameters, resultSet));
    }

    private ArrayList<LookupElement> createCompletions(AutocompleteResponse completions, @NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet) {
        ArrayList<LookupElement> elements = new ArrayList<>();
        final Lookup activeLookup = LookupManager.getActiveLookup(parameters.getEditor());
        for (int index = 0; index < completions.results.length && index < completionLimit(parameters, resultSet, completions.is_locked); index++) {
            LookupElement lookupElement = createCompletion(
                    parameters, resultSet, completions.old_prefix,
                    completions.results[index], index, completions.is_locked, activeLookup);

            if (resultSet.getPrefixMatcher().prefixMatches(lookupElement)) {
                elements.add(lookupElement);
            }
        }

        return elements;
    }

    private int completionLimit(CompletionParameters parameters, CompletionResultSet result, boolean isLocked) {
        if (isLocked) {
            return 1;
        }
        boolean preferTabNine = !endsWithADot(
                parameters.getEditor().getDocument(),
                parameters.getOffset() - result.getPrefixMatcher().getPrefix().length()
        );

        return preferTabNine ? MAX_COMPLETIONS : 1;
    }

    @NotNull
    private LookupElement createCompletion(CompletionParameters parameters, CompletionResultSet resultSet,
                                                  String oldPrefix, ResultEntry result, int index,
                                           boolean locked, @Nullable Lookup activeLookup) {
        TabNineCompletion completion = new TabNineCompletion(
                oldPrefix,
                result.new_prefix,
                result.old_suffix,
                result.new_suffix,
                index,
                resultSet.getPrefixMatcher().getPrefix(),
                getCursorPrefix(parameters),
                getCursorSuffix(parameters),
                result.origin
        );

        completion.detail = result.detail;

        if (result.deprecated != null) {
            completion.deprecated = result.deprecated;
        }

        LookupElementBuilder lookupElementBuilder =
            LookupElementBuilder.create(completion, result.new_prefix)
            .withRenderer(
                new LookupElementRenderer<LookupElement>() {
                  @Override
                  public void renderElement(
                      LookupElement element, LookupElementPresentation presentation) {
                    TabNineCompletion lookupElement = (TabNineCompletion) element.getObject();
                    String typeText = (locked ? LIMITATION_SYMBOL : "");
                    if (Config.DISPLAY_ORIGIN) {
                      typeText += lookupElement.origin.toString();
                    } else {
                      typeText += StaticConfig.BRAND_NAME;
                    }
                    presentation.setTypeText(typeText);
                    presentation.setItemTextBold(false);
                    presentation.setStrikeout(lookupElement.deprecated);
                    presentation.setItemText(lookupElement.newPrefix);
                    presentation.setIcon(ICON);
                  }
                });
        if (locked) {
            final LimitExceededLookupElement lookupElement = new LimitExceededLookupElement(
                    lookupElementBuilder);
            if (activeLookup != null) {
                activeLookup.addLookupListener(lookupElement);
            }
            return lookupElement;
        } else {
      lookupElementBuilder =
          lookupElementBuilder.withInsertHandler(
              (context, item) -> {
                final Project project = context.getProject();
                final Document document = context.getDocument();
                final Editor editor = context.getEditor();
                final PsiFile file = context.getFile();
                int end = context.getTailOffset();
                int start = context.getStartOffset();
                TabNineCompletion lookupElement = (TabNineCompletion) item.getObject();
                try {
                  document.insertString(
                      end + lookupElement.oldSuffix.length(), lookupElement.newSuffix);
                  document.deleteString(end, end + lookupElement.oldSuffix.length());
                  System.out.println(
                      "--> Context start is " + start + " and context end is " + end);
                  System.out.println("--> Current offset is " + editor.getCaretModel().getOffset());
                  System.out.println(
                      "--> Total length is "
                          + (lookupElement.newPrefix + lookupElement.newSuffix).length());
                  System.out.println(
                      "--> Computed start is "
                          + (end - lookupElement.newPrefix.length())
                          + " and computed end is "
                          + (end + lookupElement.newSuffix.length()));

                  Key<Boolean> myKey = Key.create("eransho");
                  Boolean eransho = editor.getUserData(myKey);
                  Disposable myDisposable = Disposer.newDisposable();
                  EditorUtil.disposeWithEditor(editor, myDisposable);

                  AutoImporter.registerTabNineAutoImporter(context, item);

                  //                    String insertedText = document.getText(new TextRange(start,
                  // end));
                  //                    Set<String> termsSet =
                  // Arrays.stream(insertedText.split("\\w+")).collect(Collectors.toSet());
                  //
                  //                    if (eransho == null || eransho == Boolean.FALSE) {
                  //                        ((MarkupModelEx) ((EditorEx)
                  // editor).getFilteredDocumentMarkupModel()).addMarkupModelListener(myDisposable,
                  // new MarkupModelListener() {
                  //                            @Override
                  //                            public void afterAdded(@NotNull RangeHighlighterEx
                  // highlighter) {
                  //                                Object errorTooltip =
                  // highlighter.getErrorStripeTooltip();
                  //                                HighlightInfo highlightInfo =
                  // ObjectUtils.tryCast(errorTooltip, HighlightInfo.class);
                  //                                if (highlightInfo == null ||
                  // highlightInfo.getSeverity() != HighlightSeverity.ERROR) {
                  //                                    return;
                  //                                }
                  //                                System.out.println("--> highlighter is " +
                  // highlighter);
                  //                                System.out.println("--> lookupElement is " +
                  // lookupElement.newPrefix + ", " + lookupElement.newSuffix + " and " +
                  // lookupElement.oldPrefix + ", " + lookupElement.oldSuffix);
                  //                                if (start <=
                  // highlighter.getAffectedAreaStartOffset() && end >=
                  // highlighter.getAffectedAreaEndOffset()) {
                  //                                    final DaemonCodeAnalyzerImpl codeAnalyzer =
                  // (DaemonCodeAnalyzerImpl) DaemonCodeAnalyzer.getInstance(project);
                  //
                  // CommandProcessor.getInstance().runUndoTransparentAction(() ->
                  // codeAnalyzer.autoImportReferenceAtCursor(editor, file));
                  //
                  // List<HighlightInfo.IntentionActionDescriptor> availableFixes =
                  // ShowIntentionsPass.getAvailableFixes(editor, file, -1,
                  // highlighter.getEndOffset());
                  //
                  // List<HighlightInfo.IntentionActionDescriptor> filteredFixes =
                  // availableFixes.stream().filter(f ->
                  // f.getAction().getText().toLowerCase().contains("import")).collect(Collectors.toList());
                  //                                    System.out.println(filteredFixes);
                  //
                  ////                                    availableFixes.stream().filter(f ->
                  // f.getAction().getText().toLowerCase().contains("import")).findFirst().ifPresent(fix -> {
                  ////
                  // PsiDocumentManager.getInstance(project).commitDocument(highlighter.getDocument());
                  ////
                  // ShowIntentionActionsHandler.chooseActionAndInvoke(file, editor,
                  // fix.getAction(), fix.getAction().getText());
                  ////                                        Disposer.dispose(myDisposable);
                  ////                                    });
                  //                                }
                  //                            }
                  //                        });
                  //                        editor.putUserData(myKey, Boolean.TRUE);
                  //                    }

                } catch (RuntimeException re) {
                  Logger.getInstance(getClass())
                      .warn(
                          "Error inserting new suffix. End = "
                              + end
                              + ", old suffix length = "
                              + lookupElement.oldSuffix.length()
                              + ", new suffix length = "
                              + lookupElement.newSuffix.length(),
                          re);
                }
              });
        }
        return lookupElementBuilder;
    }

    private void addAdvertisement(@NotNull CompletionResultSet result, AutocompleteResponse completions) {
        if (completions.user_message.length >= 1) {
            String details = String.join(" ", completions.user_message);

            details = details.substring(0, Math.min(details.length(), ADVERTISEMENT_MAX_LENGTH));

            result.addLookupAdvertisement(details);
        }
    }

    private String getCursorPrefix(CompletionParameters parameters) {
        Document document = parameters.getEditor().getDocument();
        int cursorPosition = parameters.getOffset();
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineStart = document.getLineStartOffset(lineNumber);

        return document.getText(TextRange.create(lineStart, cursorPosition)).trim();
    }

    private String getCursorSuffix(CompletionParameters parameters) {
        Document document = parameters.getEditor().getDocument();
        int cursorPosition = parameters.getOffset();
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineEnd = document.getLineEndOffset(lineNumber);

        return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    }

    private void registerLookupListener(CompletionParameters parameters) {
        final LookupEx lookupEx = LookupManager.getActiveLookup(parameters.getEditor());
        if (lookupEx == null) {
            return;
        }
        lookupEx.removeLookupListener(tabNineLookupListener);
        lookupEx.addLookupListener(tabNineLookupListener);
    }
}
