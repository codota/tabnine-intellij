package com.tabnine;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.tabnine.binary.TabNineProcess;
import com.tabnine.contracts.AutocompleteRequest;
import com.tabnine.contracts.AutocompleteResponse;
import com.tabnine.prediction.TabNineLookupElement;
import com.tabnine.prediction.TabNinePrefixMatcher;
import com.tabnine.prediction.TabNineWeigher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.tabnine.StaticConfig.*;
import static com.tabnine.Utils.endsWithADot;

public class TabNineCompletionContributor extends CompletionContributor {
    private TabNineProcess process;

    TabNineCompletionContributor() {
        process = new TabNineProcess();

        process.init();
    }


    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        boolean preferTabNine = !endsWithADot(
                parameters.getEditor().getDocument(),
                parameters.getOffset() - result.getPrefixMatcher().getPrefix().length()
        );
        AutocompleteResponse completions = this.retrieveCompletions(parameters);
        PrefixMatcher originalMatcher = result.getPrefixMatcher();
        if (completions != null) {
            if (originalMatcher.getPrefix().length() == 0 && completions.results.length == 0) {
                return;
            }
            result = result.withPrefixMatcher(new TabNinePrefixMatcher(originalMatcher.cloneWithPrefix(completions.old_prefix)));
            result = result.withRelevanceSorter(CompletionSorter.defaultSorter(parameters, originalMatcher).weigh(new TabNineWeigher()));
            result.restartCompletionOnAnyPrefixChange();
            if (completions.user_message.length >= 1) {
                String details = String.join(" ", completions.user_message);
                if (details.length() > StaticConfig.ADVERTISEMENT_MAX_LENGTH){
                    details = details.substring(0, StaticConfig.ADVERTISEMENT_MAX_LENGTH);
                }
                result.addLookupAdvertisement(details);
            }
            ArrayList<LookupElement> elements = new ArrayList<>();
            int maxCompletions = preferTabNine ? MAX_COMPLETIONS : 1;
            for (int i = 0; i < completions.results.length && i < maxCompletions; i++) {
                TabNineLookupElement elt = new TabNineLookupElement(
                        i,
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

    private AutocompleteResponse retrieveCompletions(CompletionParameters parameters) {
        try {
            if (this.process.isStarting()) {
                Logger.getInstance(getClass()).info("Can't get completions because TabNine process is not started yet.");
                return null;
            }

            return ApplicationUtil.runWithCheckCanceled(() -> {
                Document doc = parameters.getEditor().getDocument();
                int middle = parameters.getOffset();
                int begin = Integer.max(0, middle - MAX_OFFSET);
                int end = Integer.min(doc.getTextLength(), middle + MAX_OFFSET);
                AutocompleteRequest req = new AutocompleteRequest();
                req.before = doc.getText(new TextRange(begin, middle));
                req.after = doc.getText(new TextRange(middle, end));
                req.filename = parameters.getOriginalFile().getVirtualFile().getPath();
                req.max_num_results = MAX_COMPLETIONS;
                req.region_includes_beginning = (begin == 0);
                req.region_includes_end = (end == doc.getTextLength());

                try {
                    return ApplicationManager.getApplication()
                            .executeOnPooledThread(() -> this.process.request(req))
                            .get(COMPLETION_TIME_THRESHOLD, TimeUnit.MILLISECONDS);
                } catch (ExecutionException e) {
                    Logger.getInstance(getClass()).warn("TabNine is in invalid state, it is being restarted.", e);

                    this.process.restart();
                } catch (Throwable e) {
                    Logger.getInstance(getClass()).info("TabNine's response timed out.");
                }

                return null;
            }, ProgressManager.getInstance().getProgressIndicator());
        } catch (Exception e) {
            return null;
        }
    }
}
