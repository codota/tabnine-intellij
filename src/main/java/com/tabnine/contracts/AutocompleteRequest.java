package com.tabnine.contracts;

import com.google.gson.annotations.SerializedName;
import com.tabnine.StaticConfig;
import com.tabnine.binary.BinaryRequest;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

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

    public boolean validate(@NotNull AutocompleteResponse response) {
        return this.before.endsWith(response.old_prefix);
    }

    public Map<String, Object> serialize(int correlationId) {
        this.correlationId = correlationId;

        Map<String, Object> jsonObject = new HashMap<>();

        jsonObject.put("version", StaticConfig.BINARY_PROTOCOL_VERSION);
        jsonObject.put("request", singletonMap("Autocomplete", this));

        return jsonObject;
    }
}
