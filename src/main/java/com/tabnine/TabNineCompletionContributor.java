package com.tabnine;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.tabnine.binary.TabNineGateway;
import com.tabnine.binary.TabnineGatewayWrapper;
import com.tabnine.contracts.AutocompleteRequest;
import com.tabnine.contracts.AutocompleteResponse;
import com.tabnine.prediction.TabNineLookupElement;
import com.tabnine.prediction.TabNinePrefixMatcher;
import com.tabnine.prediction.TabNineWeigher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import static com.tabnine.StaticConfig.MAX_COMPLETIONS;
import static com.tabnine.StaticConfig.MAX_OFFSET;
import static com.tabnine.Utils.endsWithADot;

public class TabNineCompletionContributor extends CompletionContributor {
    private TabNineGateway gateway = TabnineGatewayWrapper.getOrCreateInstance();

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return true;
    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        registerLookupListener(parameters);
        boolean preferTabNine = !endsWithADot(
                parameters.getEditor().getDocument(),
                parameters.getOffset() - result.getPrefixMatcher().getPrefix().length()
        );
        AutocompleteResponse completions = this.retrieveCompletions(parameters);
        PrefixMatcher originalMatcher = result.getPrefixMatcher();
        if (completions != null) {
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
            if (originalMatcher.getPrefix().length() == 0 && completions.results.length == 0) {
                result.stopHere();
                return;
            }
            ArrayList<LookupElement> elements = new ArrayList<>();
            int maxCompletions = preferTabNine ? MAX_COMPLETIONS : 1;
            for (int i = 0; i < completions.results.length && i < maxCompletions; i++) {
                TabNineLookupElement elt = new TabNineLookupElement(
                        i,
                        completions.results[i].origin,
                        // TODO: ADD CompletionOrigin here from complteion.origin
                        completions.old_prefix,
                        completions.results[i].new_prefix,
                        completions.results[i].old_suffix,
                        completions.results[i].new_suffix
                );
                elt.copyLspFrom(completions.results[i]);

                if (result.getPrefixMatcher().prefixMatches(elt)) {
                    elements.add(elt);
                }
            }
            result.addAllElements(elements);
        }
    }

    private void registerLookupListener(CompletionParameters parameters) {
        LookupEx lookupEx = Objects.requireNonNull(LookupManager.getInstance(Objects.requireNonNull(parameters.getEditor().getProject())).getActiveLookup());
        TabNineLookupListener tabNineLookupListener = TabNineLookupListener.getOrCreate();
        lookupEx.removeLookupListener(tabNineLookupListener);
        lookupEx.addLookupListener(tabNineLookupListener);
    }

    private AutocompleteResponse retrieveCompletions(CompletionParameters parameters) {
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

                return this.gateway.request(req);
            }, ProgressManager.getInstance().getProgressIndicator());
        } catch (Exception e) {
            return null;
        }
    }
}
