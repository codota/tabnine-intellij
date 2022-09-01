package com.tabnine.inline;

import com.tabnine.binary.requests.autocomplete.AutocompleteRequest;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;

public interface CompletionAdjustment {
  void adjustRequest(AutocompleteRequest autocompleteRequest);

  void adjustResponse(AutocompleteResponse autocompleteResponse);

  CompletionAdjustmentType getType();
}
