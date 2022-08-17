package com.tabnine.binary.requests.autocomplete;

import com.tabnine.general.CompletionKind;
import com.tabnine.general.CompletionOrigin;
import com.tabnine.intellij.completions.Completion;

public class ResultEntry implements Completion {
  public String new_prefix;
  public String old_suffix;
  public String new_suffix;

  public CompletionOrigin origin;
  public String detail;
  public Boolean deprecated;
  public CompletionKind completion_kind;
  public Boolean is_cached;
  // TODO other lsp types

  @Override
  public boolean isSnippet() {
    return this.completion_kind == CompletionKind.Snippet;
  }
}
