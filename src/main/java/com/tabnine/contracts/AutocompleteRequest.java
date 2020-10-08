package com.tabnine.contracts;

import com.google.gson.annotations.SerializedName;
import com.tabnine.selections.BinaryRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.tabnine.general.StaticConfig.wrapWithBinaryRequest;
import static java.util.Collections.singletonMap;

public class AutocompleteRequest implements BinaryRequest<Map<String, Object>, AutocompleteResponse> {
    public String before;
    public String after;
    public String filename;
    @SerializedName(value = "region_includes_beginning")
    public boolean regionIncludesBeginning;
    @SerializedName(value = "region_includes_end")
    public boolean regionIncludesEnd;
    @SerializedName(value = "max_num_results")
    public int maxResults;
    @SerializedName(value = "correlation_id")
    public Integer correlationId;

    public Class<AutocompleteResponse> response() {
        return AutocompleteResponse.class;
    }

    @Override
    public Map<String, Object> serialize(int correlationId) {
        this.correlationId = correlationId;

        return wrapWithBinaryRequest(singletonMap("Autocomplete", this));
    }

    public boolean validate(@NotNull AutocompleteResponse response) {
        return this.before.endsWith(response.old_prefix);
    }
}
