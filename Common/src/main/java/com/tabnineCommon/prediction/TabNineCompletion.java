package com.tabnineCommon.prediction;

import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.FList;
import com.tabnineCommon.binary.requests.autocomplete.CompletionMetadata;
import com.tabnineCommon.general.CompletionKind;
import com.tabnineCommon.general.CompletionOrigin;
import com.tabnineCommon.general.SuggestionTrigger;
import com.tabnineCommon.intellij.completions.Completion;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class TabNineCompletion implements Completion {
  public final String oldPrefix;
  public final String newPrefix;
  public final String oldSuffix;
  public final String newSuffix;
  public final int index;
  public String cursorPrefix;
  public String cursorSuffix;
  public SuggestionTrigger suggestionTrigger;
  @Nullable // if new plugin with old binary
  public CompletionMetadata completionMetadata;
  private String fullSuffix = null;

  public TabNineCompletion(
      String oldPrefix,
      String newPrefix,
      String oldSuffix,
      String newSuffix,
      int index,
      String cursorPrefix,
      String cursorSuffix,
      @Nullable CompletionMetadata completionMetadata,
      SuggestionTrigger suggestionTrigger) {
    this.oldPrefix = oldPrefix;
    this.newPrefix = newPrefix;
    this.oldSuffix = oldSuffix;
    this.newSuffix = newSuffix;
    this.index = index;
    this.cursorPrefix = cursorPrefix;
    this.cursorSuffix = cursorSuffix;
    this.completionMetadata = completionMetadata;
    this.suggestionTrigger = suggestionTrigger;
  }

  public TabNineCompletion createAdjustedCompletion(String oldPrefix, String cursorPrefix) {
    return new TabNineCompletion(
        oldPrefix,
        this.newPrefix,
        this.oldSuffix,
        this.newSuffix,
        this.index,
        cursorPrefix,
        this.cursorSuffix,
        this.completionMetadata,
        this.suggestionTrigger);
  }

  @Nullable
  public CompletionOrigin getOrigin() {
    return completionMetadata != null ? completionMetadata.getOrigin() : null;
  }

  public String getSuffix() {
    if (fullSuffix != null) {
      return fullSuffix;
    }

    String itemText = this.newPrefix + this.newSuffix;
    String prefix = this.oldPrefix;
    if (prefix.isEmpty()) {
      return fullSuffix = itemText;
    }

    FList<TextRange> fragments = LookupCellRenderer.getMatchingFragments(prefix, itemText);
    if (fragments != null && !fragments.isEmpty()) {
      List<TextRange> list = new ArrayList<>(fragments);
      return fullSuffix = itemText.substring(list.get(list.size() - 1).getEndOffset());
    }

    return fullSuffix = "";
  }

  public int getNetLength() {
    return getSuffix().length();
  }

  @Override
  public boolean isSnippet() {
    if (this.completionMetadata == null || this.completionMetadata.getCompletion_kind() == null) {
      return false;
    }

    return this.completionMetadata.getCompletion_kind() == CompletionKind.Snippet;
  }
}
