package com.tabnine.inline;

import com.tabnine.binary.requests.autocomplete.AutocompleteRequest;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.binary.requests.autocomplete.ResultEntry;
import java.util.Arrays;

public class LookAheadCompletionAdjustment implements CompletionAdjustment {
  private final String userPrefix;
  private final String focusedCompletion;

  public LookAheadCompletionAdjustment(String userPrefix, String focusedCompletion) {
    this.userPrefix = userPrefix;
    this.focusedCompletion = focusedCompletion;
  }

  @Override
  public void adjustRequest(AutocompleteRequest autocompleteRequest) {
    autocompleteRequest.before =
        autocompleteRequest.before.substring(
                0, autocompleteRequest.before.length() - userPrefix.length())
            + focusedCompletion;
  }

  @Override
  public void adjustResponse(AutocompleteResponse autocompleteResponse) {
    autocompleteResponse.old_prefix = userPrefix;
    autocompleteResponse.results =
        Arrays.stream(autocompleteResponse.results)
            .filter(resultEntry -> resultEntry.new_prefix.startsWith(focusedCompletion))
            .toArray(ResultEntry[]::new);
  }

  @Override
  public CompletionAdjustmentType getType() {
    return CompletionAdjustmentType.LookAhead;
  }
}
