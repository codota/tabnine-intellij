package com.tabnineCommon.binary.requests.autocomplete;

import static java.util.Collections.singletonMap;

import com.google.gson.annotations.SerializedName;
import com.tabnineCommon.binary.BinaryRequest;
import org.jetbrains.annotations.Nullable;

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

  public int offset;
  public int line;
  public int character;

  @Nullable public Integer indentation_size;

  @Nullable public Boolean cached_only;

  @SerializedName(value = "sdk_path")
  public String sdkPath;

  public Class<AutocompleteResponse> response() {
    return AutocompleteResponse.class;
  }

  @Override
  public Object serialize() {
    return singletonMap("Autocomplete", this);
  }

  public boolean validate(AutocompleteResponse response) {
    return this.before.endsWith(response.old_prefix);
  }
}
