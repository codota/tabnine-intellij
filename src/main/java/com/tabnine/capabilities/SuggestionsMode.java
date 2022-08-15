package com.tabnine.capabilities;

public enum SuggestionsMode {
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
