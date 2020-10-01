package com.tabnine.contracts;

import org.jetbrains.annotations.NotNull;

public class AutocompleteRequest {
    public String before;
    public String after;
    public String filename;
    public boolean region_includes_beginning;
    public boolean region_includes_end;
    public int max_num_results;
    public Integer correlation_id;

    public String name() {
        return "Autocomplete";
    }

    public AutocompleteRequest withCorrelationId(int correlationId) {
        this.correlation_id = correlationId;

        return this;
    }

    public Class<AutocompleteResponse> response() {
        return AutocompleteResponse.class;
    }

    public boolean validate(@NotNull AutocompleteResponse response) {
        return this.before.endsWith(response.old_prefix);
    }
}
