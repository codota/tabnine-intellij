package com.tabnine;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.ResultEntry;
import com.tabnine.general.DependencyContainer;
import com.tabnine.general.StaticConfig;
import com.tabnine.lifecycle.BinaryStateService;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.prediction.TabNinePrefixMatcher;
import com.tabnine.prediction.TabNineWeigher;
import com.tabnine.selections.TabNineLookupListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import static com.tabnine.general.StaticConfig.*;
import static com.tabnine.general.Utils.endsWithADot;

public class TabNineCompletionContributor extends CompletionContributor {
    private final CompletionFacade completionFacade = DependencyContainer.instanceOfCompletionFacade();
    private final TabNineLookupListener tabNineLookupListener = DependencyContainer.instanceOfTabNineLookupListener();
    private final BinaryStateService binaryStateService = ServiceManager.getService(BinaryStateService.class);

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
        this.binaryStateService.limited(completions.is_locked);

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
                    final String typeText = (locked ? LIMITATION_SYMBOL : "") + StaticConfig.BRAND_NAME;
                    presentation.setTypeText(typeText);
                    presentation.setItemTextBold(false);
                    presentation.setStrikeout(lookupElement.deprecated);
                    presentation.setItemText(lookupElement.newPrefix);
                    presentation.setIcon(ICON);
                  }
                });
        if (locked) {
            final InsertNothingLookupElement lookupElement = new LimitExceededLookupElement(
                    lookupElementBuilder, oldPrefix);
            if (activeLookup != null) {
                activeLookup.addLookupListener(lookupElement);
            }
            return lookupElement;
        } else {
            lookupElementBuilder = lookupElementBuilder.withInsertHandler((context, item) -> {
                int end = context.getTailOffset();
                TabNineCompletion lookupElement = (TabNineCompletion) item.getObject();
                try {
                    context.getDocument().insertString(end + lookupElement.oldSuffix.length(), lookupElement.newSuffix);
                    context.getDocument().deleteString(end, end + lookupElement.oldSuffix.length());
                } catch(RuntimeException re) {
                    Logger.getInstance(getClass()).warn("Error inserting new suffix. End = " + end +
                            ", old suffix length = " + lookupElement.oldSuffix.length() + ", new suffix length = "
                            + lookupElement.newSuffix.length(), re);
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
        LookupEx lookupEx = Objects.requireNonNull(LookupManager.getInstance(Objects.requireNonNull(parameters.getEditor().getProject())).getActiveLookup());

        lookupEx.removeLookupListener(tabNineLookupListener);
        lookupEx.addLookupListener(tabNineLookupListener);
    }
}
