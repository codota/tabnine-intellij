package com.tabnine.binary.requests.autocomplete;

import com.tabnine.general.CompletionKind;
import com.tabnine.general.CompletionOrigin;

public class ResultEntry {
    public String new_prefix;
    public String old_suffix;
    public String new_suffix;

    public CompletionOrigin origin;
    public String detail;
    public Boolean deprecated;
    public CompletionKind completion_kind;
    // TODO other lsp types
}
