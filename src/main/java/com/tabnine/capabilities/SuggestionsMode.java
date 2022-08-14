package com.tabnine.capabilities;

import com.intellij.openapi.util.registry.Registry;

public enum SuggestionsMode {
  INLINE(true, false),
  AUTOCOMPLETE(false, true),
  HYBRID(true, true);

  public final boolean inlineEnabled;
  public final boolean popupEnabled;

  private SuggestionsMode(boolean inlineEnabled, boolean popupEnabled) {
    this.inlineEnabled = inlineEnabled;
    this.popupEnabled = popupEnabled;
  }

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
