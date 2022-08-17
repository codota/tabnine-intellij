package com.tabnine.selections;

import static java.util.stream.Collectors.toList;

import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.selection.SelectionRequest;
import com.tabnine.binary.requests.selection.SetStateBinaryRequest;
import com.tabnine.capabilities.SuggestionsModeService;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.statusBar.StatusBarUpdater;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TabNineLookupListener implements LookupListener {
  private final BinaryRequestFacade binaryRequestFacade;
  private final StatusBarUpdater statusBarUpdater;
  private final SuggestionsModeService suggestionsModeService;

  public TabNineLookupListener(
      BinaryRequestFacade binaryRequestFacade,
      StatusBarUpdater statusBarUpdater,
      SuggestionsModeService suggestionsModeService) {
    this.binaryRequestFacade = binaryRequestFacade;
    this.statusBarUpdater = statusBarUpdater;
    this.suggestionsModeService = suggestionsModeService;
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
      List<TabNineCompletion> suggestions =
          event.getLookup().getItems().stream()
              .map(
                  l -> {
                    try {
                      return l.getObject();
                    } catch (RuntimeException re) {
                      return null;
                    }
                  })
              .filter(TabNineCompletion.class::isInstance)
              .map(TabNineCompletion.class::cast)
              .collect(toList());

      SelectionRequest selection = new SelectionRequest();

      selection.language = SelectionUtil.asLanguage(event.getLookup().getPsiFile().getName());
      selection.netLength = item.newPrefix.replaceFirst("^" + item.oldPrefix, "").length();
      selection.linePrefixLength = item.cursorPrefix.length();
      selection.lineNetPrefixLength = selection.linePrefixLength - item.oldPrefix.length();
      selection.lineSuffixLength = item.cursorSuffix.length();
      selection.index = ((LookupImpl) event.getLookup()).getSelectedIndex();
      selection.origin = item.origin;
      selection.length = item.newPrefix.length();
      selection.strength = SelectionUtil.getStrength(item);
      selection.completionKind = item.completionKind;
      selection.snippetContext = item.snippet_context;
      selection.suggestionRenderingMode = suggestionsModeService.getSuggestionMode();
      SelectionUtil.addSuggestionsCount(selection, suggestions);

      binaryRequestFacade.executeRequest(new SetStateBinaryRequest(selection));
      this.statusBarUpdater.updateStatusBar();
    }
  }
}
