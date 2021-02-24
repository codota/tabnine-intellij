package com.tabnine.selections;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.selection.SelectionRequest;
import com.tabnine.binary.requests.selection.SelectionSuggestionRequest;
import com.tabnine.binary.requests.selection.SetStateBinaryRequest;
import com.tabnine.general.CompletionOrigin;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.statusBar.StatusBarUpdater;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.tabnine.general.Utils.toInt;
import static java.util.stream.Collectors.*;

public class TabNineLookupListener implements LookupListener {
    private final BinaryRequestFacade binaryRequestFacade;
    private final StatusBarUpdater statusBarUpdater;

    public TabNineLookupListener(BinaryRequestFacade binaryRequestFacade) {
        this.binaryRequestFacade = binaryRequestFacade;
        this.statusBarUpdater = new StatusBarUpdater(binaryRequestFacade);
    }

    @Override
    public void currentItemChanged(@NotNull LookupEvent event) {
        // Do nothing, but the validator is furious if we don't implement this.
        // Probably because in older versions this was not implemented.
    }

    @Override
    public void lookupCanceled(@NotNull LookupEvent event) {
        // Do nothing, but the validator is furious if we don't implement this.
        // Probably because in older versions this was not implemented.
    }

    @Override
    public void itemSelected(@NotNull LookupEvent event) {
        if (event.isCanceledExplicitly()) {
            return;
        }

        if (event.getItem() != null && event.getItem().getObject() instanceof TabNineCompletion) {
            // They picked us, yay!
            TabNineCompletion item = (TabNineCompletion) event.getItem().getObject();
            List<TabNineCompletion> suggestions = event.getLookup().getItems().stream()
                    .map(LookupElement::getObject)
                    .filter(TabNineCompletion.class::isInstance)
                    .map(TabNineCompletion.class::cast).collect(toList());

            SelectionRequest selection = new SelectionRequest();

            selection.language = asLanguage(event.getLookup().getPsiFile().getName());
            selection.netLength = item.newPrefix.replaceFirst("^" + item.completionPrefix, "").length();
            selection.linePrefixLength = item.cursorPrefix.length();
            selection.lineNetPrefixLength = selection.linePrefixLength - item.completionPrefix.length();
            selection.lineSuffixLength = item.cursorSuffix.length();
            selection.index = ((LookupImpl) event.getLookup()).getSelectedIndex();
            selection.origin = item.origin;
            selection.length = item.newPrefix.length();
            selection.strength = getStrength(item);
            addSuggestionsCount(selection, suggestions);

            binaryRequestFacade.executeRequest(new SetStateBinaryRequest(selection));
            this.statusBarUpdater.updateStatusBar();
        }
    }

    private String getStrength(TabNineCompletion item) {
        if (item.origin == CompletionOrigin.LSP) {
            return null;
        }

        return item.detail;
    }

    private void addSuggestionsCount(SelectionRequest selection, List<TabNineCompletion> suggestions) {
        Map<CompletionOrigin, Long> originCount = suggestions.stream()
                .collect(groupingBy(TabNineCompletion::getOrigin, counting()));

        selection.suggestionsCount = suggestions.size();
        selection.deepCloudSuggestionsCount = toInt(originCount.get(CompletionOrigin.CLOUD));
        selection.deepLocalSuggestionsCount = toInt(originCount.get(CompletionOrigin.LOCAL));
        selection.lspSuggestionsCount = toInt(originCount.get(CompletionOrigin.LSP));
        selection.vanillaSuggestionsCount = toInt(originCount.get(CompletionOrigin.VANILLA));

        selection.suggestions = suggestions.stream()
                .map(suggestion -> new SelectionSuggestionRequest(suggestion.newPrefix.length(), getStrength(suggestion), suggestion.origin.name()))
                .collect(toList());
    }

    @Nonnull
    private String asLanguage(String name) {
        String[] split = name.split("\\.");

        return Arrays.stream(split).skip(Math.max(1, split.length - 1)).findAny().orElse("undefined");
    }
}
