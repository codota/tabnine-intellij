package com.tabnine.prediction;

import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.FList;
import com.tabnine.binary.requests.autocomplete.SnippetContext;
import com.tabnine.general.CompletionKind;
import com.tabnine.general.CompletionOrigin;
import com.tabnine.general.SuggestionTrigger;
import com.tabnine.intellij.completions.Completion;
import java.util.ArrayList;
import java.util.List;

public class TabNineCompletion implements Completion {
  public final String oldPrefix;
  public final String newPrefix;
  public final String oldSuffix;
  public final String newSuffix;
  public final int index;
  public String cursorPrefix;
  public String cursorSuffix;
  public CompletionOrigin origin;
  public CompletionKind completionKind;
  public SuggestionTrigger suggestionTrigger;
  public Boolean isCached;
  public SnippetContext snippet_context;

  public String detail = null;
  public boolean deprecated = false;
  private String fullSuffix = null;

  public TabNineCompletion(
      String oldPrefix,
      String newPrefix,
      String oldSuffix,
      String newSuffix,
      int index,
      String cursorPrefix,
      String cursorSuffix,
      CompletionOrigin origin,
      CompletionKind completionKind,
      Boolean isCached,
      SnippetContext snippet_context,
      SuggestionTrigger suggestionTrigger) {
    this.oldPrefix = oldPrefix;
    this.newPrefix = newPrefix;
    this.oldSuffix = oldSuffix;
    this.newSuffix = newSuffix;
    this.index = index;
    this.cursorPrefix = cursorPrefix;
    this.cursorSuffix = cursorSuffix;
    this.origin = origin;
    this.completionKind = completionKind;
    this.isCached = isCached;
    this.snippet_context = snippet_context;
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
        this.origin,
        this.completionKind,
        true,
        this.snippet_context,
        this.suggestionTrigger);
  }

  public CompletionOrigin getOrigin() {
    return origin;
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
    return this.completionKind == CompletionKind.Snippet;
  }
}
