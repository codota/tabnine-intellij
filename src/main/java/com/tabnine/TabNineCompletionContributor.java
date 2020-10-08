package com.tabnine;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.TabNineGateway;
import com.tabnine.contracts.AutocompleteRequest;
import com.tabnine.contracts.AutocompleteResponse;
import com.tabnine.general.DependencyContainer;
import com.tabnine.general.StaticConfig;
import com.tabnine.prediction.TabNineLookupElement;
import com.tabnine.prediction.TabNinePrefixMatcher;
import com.tabnine.prediction.TabNineWeigher;
import com.tabnine.selections.TabNineLookupListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.tabnine.general.StaticConfig.*;
import static com.tabnine.general.Utils.endsWithADot;

public class TabNineCompletionContributor extends CompletionContributor {
    private final TabNineGateway process = DependencyContainer.singletonOfTabNineGateway();

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        int cursorPosition = parameters.getOffset();
        boolean preferTabNine = !endsWithADot(
                parameters.getEditor().getDocument(),
                cursorPosition - result.getPrefixMatcher().getPrefix().length()
        );
        AutocompleteResponse completions = this.retrieveCompletions(parameters);
        if (completions != null) {
            PrefixMatcher originalMatcher = result.getPrefixMatcher();
            if (originalMatcher.getPrefix().length() == 0 && completions.results.length == 0) {
                return;
            }
            result = result.withPrefixMatcher(new TabNinePrefixMatcher(originalMatcher.cloneWithPrefix(completions.old_prefix)));
            result = result.withRelevanceSorter(CompletionSorter.defaultSorter(parameters, originalMatcher).weigh(new TabNineWeigher()));
            result.restartCompletionOnAnyPrefixChange();
            if (completions.user_message.length >= 1) {
                String details = String.join(" ", completions.user_message);
                if (details.length() > StaticConfig.ADVERTISEMENT_MAX_LENGTH) {
                    details = details.substring(0, StaticConfig.ADVERTISEMENT_MAX_LENGTH);
                }
                result.addLookupAdvertisement(details);
            }
            ArrayList<LookupElement> elements = new ArrayList<>();
            int maxCompletions = preferTabNine ? MAX_COMPLETIONS : 1;
            for (int i = 0; i < completions.results.length && i < maxCompletions; i++) {
                TabNineLookupElement elt = new TabNineLookupElement(
                        i,
                        completions.results[i].origin,
                        completions.old_prefix,
                        completions.results[i].new_prefix,
                        completions.results[i].old_suffix,
                        completions.results[i].new_suffix
                );
                elt.withCompletionPrefix(result.getPrefixMatcher().getPrefix())
                        .withCursorPrefix(getCursorPrefix(parameters.getEditor().getDocument(), cursorPosition))
                        .withCursorSuffix(getCursorSuffix(parameters.getEditor().getDocument(), cursorPosition));
                elt.copyLspFrom(completions.results[i]);

                if (result.getPrefixMatcher().prefixMatches(elt)) {
                    elements.add(elt);
                }
            }
            result.addAllElements(elements);
        }
    }

    private String getCursorPrefix(Document document, int cursorPosition) {
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineStart = document.getLineStartOffset(lineNumber);

        return document.getText(TextRange.create(lineStart, cursorPosition)).trim();
    }

    private String getCursorSuffix(Document document, int cursorPosition) {
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineEnd = document.getLineEndOffset(lineNumber);

        return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    }

    private AutocompleteResponse retrieveCompletions(CompletionParameters parameters) {
        registerLookupListener(parameters);
        try {
            return ApplicationUtil.runWithCheckCanceled(() -> {
                Document doc = parameters.getEditor().getDocument();
                int middle = parameters.getOffset();
                int begin = Integer.max(0, middle - MAX_OFFSET);
                int end = Integer.min(doc.getTextLength(), middle + MAX_OFFSET);
                AutocompleteRequest req = new AutocompleteRequest();
                req.before = doc.getText(new TextRange(begin, middle));
                req.after = doc.getText(new TextRange(middle, end));
                req.filename = parameters.getOriginalFile().getVirtualFile().getPath();
                req.maxResults = MAX_COMPLETIONS;
                req.regionIncludesBeginning = (begin == 0);
                req.regionIncludesEnd = (end == doc.getTextLength());

                try {
                    return AppExecutorUtil.getAppExecutorService().submit(() -> this.process.request(req))
                            .get(COMPLETION_TIME_THRESHOLD, TimeUnit.MILLISECONDS);
                } catch (ExecutionException e) {
                    Logger.getInstance(getClass()).warn("TabNine is in invalid state, it is being restarted.", e);

                    this.process.restart();
                } catch (TimeoutException e) {
                    Logger.getInstance(getClass()).info("TabNine's response timed out.");
                } catch (Throwable t) {
                    Logger.getInstance(getClass()).error("TabNine's threw an unknown error.", t);
                }

                return null;
            }, ProgressManager.getInstance().getProgressIndicator());
        } catch (Exception e) {
            return null;
        }
    }

    private void registerLookupListener(CompletionParameters parameters) {
        LookupEx lookupEx = Objects.requireNonNull(LookupManager.getInstance(Objects.requireNonNull(parameters.getEditor().getProject())).getActiveLookup());
        TabNineLookupListener tabNineLookupListener = DependencyContainer.singletonOfTabNineLookupListener();
        lookupEx.removeLookupListener(tabNineLookupListener);
        lookupEx.addLookupListener(tabNineLookupListener);
    }
}
