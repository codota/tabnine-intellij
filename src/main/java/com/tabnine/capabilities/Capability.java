package com.tabnine.capabilities;

import com.google.gson.annotations.SerializedName;

public enum Capability {
  @SerializedName("inline_suggestions_mode")
  INLINE_SUGGESTIONS,
  @SerializedName("alpha")
  ALPHA,
}
