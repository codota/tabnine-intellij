package com.tabnine.capabilities;

import com.google.gson.annotations.SerializedName;

public enum SuggestionsMode {
  @SerializedName(value = "Inline")
  INLINE {
    @Override
    public boolean isInlineEnabled() {
      return true;
    }

    @Override
    public boolean isPopupEnabled() {
      return false;
    }
  },
  @SerializedName(value = "Popup")
  AUTOCOMPLETE {
    @Override
    public boolean isInlineEnabled() {
      return false;
    }

    @Override
    public boolean isPopupEnabled() {
      return true;
    }
  },
  @SerializedName(value = "Hybrid")
  HYBRID {
    @Override
    public boolean isInlineEnabled() {
      return true;
    }

    @Override
    public boolean isPopupEnabled() {
      return true;
    }
  };

  public abstract boolean isInlineEnabled();

  public abstract boolean isPopupEnabled();
}
