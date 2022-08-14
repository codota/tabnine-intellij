package com.tabnine.capabilities;

import com.intellij.openapi.util.registry.Registry;

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

  public static SuggestionsMode getSuggestionMode() {
    boolean jbPreviewOn =
        Registry.is(
            "ide.lookup.preview.insertion"); // If true, jetbrains build in preview feature is on
    if (jbPreviewOn) {
      return SuggestionsMode.AUTOCOMPLETE;
    }

    if (CapabilitiesService.getInstance().isCapabilityEnabled(Capability.USE_HYBRID_INLINE_POPUP)) {
      return SuggestionsMode.HYBRID;
    }

    if (CapabilitiesService.getInstance().isCapabilityEnabled(Capability.INLINE_SUGGESTIONS)) {
      return INLINE;
    }

    return AUTOCOMPLETE;
  }
}
