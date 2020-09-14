package com.tabnine;

class AutocompleteRequest implements Request<AutocompleteResponse> {
    String before;
    String after;
    String filename;
    boolean region_includes_beginning;
    boolean region_includes_end;
    int max_num_results;

    public String name() {
        return "Autocomplete";
    }

    public Class<AutocompleteResponse> response() {
        return AutocompleteResponse.class;
    }

    public boolean validate(AutocompleteResponse response) {
        return this.before.endsWith(response.old_prefix);
    }
}
