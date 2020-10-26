package com.tabnine.binary.requests.autocomplete;

import com.google.gson.annotations.SerializedName;
import com.tabnine.binary.BinaryRequest;
import org.jetbrains.annotations.NotNull;

import static com.tabnine.general.StaticConfig.wrapWithBinaryRequest;
import static java.util.Collections.singletonMap;

public class AutocompleteRequest implements BinaryRequest<AutocompleteResponse> {
    public String before;
    public String after;
    public String filename;
    @SerializedName(value = "region_includes_beginning")
    public boolean regionIncludesBeginning;
    @SerializedName(value = "region_includes_end")
    public boolean regionIncludesEnd;
    @SerializedName(value = "max_num_results")
    public int maxResults;

    public Class<AutocompleteResponse> response() {
        return AutocompleteResponse.class;
    }

    @Override
    public Object serialize() {
        return wrapWithBinaryRequest(singletonMap("Autocomplete", this));
    }

    public boolean validate(@NotNull AutocompleteResponse response) {
        return this.before.endsWith(response.old_prefix);
    }
}
