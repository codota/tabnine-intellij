package com.tabnine.capabilities;

import com.intellij.openapi.util.registry.Registry;

public enum SuggestionsMode {
  INLINE,
  AUTOCOMPLETE,
  ;

  public static SuggestionsMode getSuggestionMode() {
    boolean jbPreviewOn = Registry.is("ide.lookup.preview.insertion"); // If true, jetbrains build in preview feature is on
    if (CapabilitiesService.getInstance().isCapabilityEnabled(Capability.INLINE_SUGGESTIONS)
        && !jbPreviewOn) {
      return INLINE;
    } else {
      return AUTOCOMPLETE;
    }
  }
}
