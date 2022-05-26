package com.tabnine.inline;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.tabnine.binary.requests.selection.SelectionRequest;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.selections.SelectionUtil;
import java.util.List;

public class CompletionState {
  private static final Key<CompletionState> INLINE_COMPLETION_STATE =
      Key.create("INLINE_COMPLETION_STATE");

  private Long modificationStamp = null;
  private List<TabNineCompletion> suggestions;
  private int currentIndex = 0;

  //  public static CompletionState getOrCreateInstance(@NotNull Editor editor) {
  //    CompletionState state = editor.getUserData(INLINE_COMPLETION_STATE);
  //    if (state == null) {
  //      state = new CompletionState();
  //      editor.putUserData(INLINE_COMPLETION_STATE, state);
  //    }
  //    return state;
  //  }

  public void updateSuggestions(List<TabNineCompletion> completions, long modificationStamp) {
    Logger.getInstance(getClass()).warn("BOAZ: Updating suggestions");
    if (this.modificationStamp != null && modificationStamp < this.modificationStamp) {
      Logger.getInstance(getClass())
          .warn("BOAZ: Suggestions was not updated as modification stamp is old");
      return;
    }

    this.suggestions = completions;
    this.modificationStamp = modificationStamp;
  }

  public boolean noSuggestions() {
    return suggestions == null || suggestions.isEmpty();
  }

  public TabNineCompletion getCurrentCompletion() {
    return suggestions.get(currentIndex);
  }

  public void addSuggestionsMetadata(SelectionRequest selection) {
    selection.index = currentIndex;
    SelectionUtil.addSuggestionsCount(selection, suggestions);
  }

  public boolean changedSince(long modificationStamp) {
    return modificationStamp != this.modificationStamp;
  }

  public void toggleCompletionIndex(CompletionOrder order) {
    currentIndex =
        (suggestions.size() + currentIndex + (order == CompletionOrder.NEXT ? 1 : -1))
            % suggestions.size();
  }
}
