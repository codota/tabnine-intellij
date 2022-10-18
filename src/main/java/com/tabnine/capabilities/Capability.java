package com.tabnine.capabilities;

import com.google.gson.annotations.SerializedName;

public enum Capability {
  @SerializedName("inline_suggestions_mode")
  INLINE_SUGGESTIONS,
  @SerializedName("alpha")
  ALPHA,
  @SerializedName("first_suggestion_hint_enabled")
  FIRST_SUGGESTION_HINT_ENABLED,

  @SerializedName("use_hybrid_inline_popup")
  USE_HYBRID_INLINE_POPUP,
  @SerializedName("debounce_value_300")
  DEBOUNCE_VALUE_300,
  @SerializedName("debounce_value_600")
  DEBOUNCE_VALUE_600,
}
