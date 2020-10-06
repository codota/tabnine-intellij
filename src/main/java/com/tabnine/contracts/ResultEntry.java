package com.tabnine.contracts;

import com.tabnine.selections.CompletionOrigin;

public class ResultEntry {
    public String new_prefix;
    public String old_suffix;
    public String new_suffix;

    public CompletionOrigin origin;
    public String detail;
    public Boolean deprecated;
    // TODO other lsp types
}
