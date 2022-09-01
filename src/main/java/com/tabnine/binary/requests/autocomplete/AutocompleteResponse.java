package com.tabnine.binary.requests.autocomplete;

import com.tabnine.binary.BinaryResponse;
import org.jetbrains.annotations.Nullable;

public class AutocompleteResponse implements BinaryResponse {
  public String old_prefix;
  public ResultEntry[] results;
  public String[] user_message;
  public boolean is_locked;
  @Nullable public SnippetContext snippet_context;
}
