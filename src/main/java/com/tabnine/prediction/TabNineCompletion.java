package com.tabnine.prediction;

import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.FList;
import com.tabnine.binary.requests.autocomplete.UserIntent;
import com.tabnine.general.CompletionKind;
import com.tabnine.general.CompletionOrigin;
import java.util.ArrayList;
import java.util.List;

public class TabNineCompletion {
  public final String oldPrefix;
  public final String newPrefix;
  public final String oldSuffix;
  public final String newSuffix;
  public final int index;
  public String cursorPrefix;
  public String cursorSuffix;
  public CompletionOrigin origin;
  public CompletionKind completionKind;
  public Boolean isCached;
  public UserIntent snippet_intent;

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
      UserIntent snippet_intent) {
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
    this.snippet_intent = snippet_intent;
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

  //  FIXME: DELETE ME!!!
  @Override
  public String toString() {
    return "TabNineCompletion{"
        + "oldPrefix='"
        + oldPrefix
        + '\''
        + ", newPrefix='"
        + newPrefix
        + '\''
        + ", oldSuffix='"
        + oldSuffix
        + '\''
        + ", newSuffix='"
        + newSuffix
        + '\''
        + ", index="
        + index
        + ", cursorPrefix='"
        + cursorPrefix
        + '\''
        + ", cursorSuffix='"
        + cursorSuffix
        + '\''
        + ", origin="
        + origin
        + ", completionKind="
        + completionKind
        + ", isCached="
        + isCached
        + ", snippet_intent="
        + snippet_intent
        + ", detail='"
        + detail
        + '\''
        + ", deprecated="
        + deprecated
        + ", fullSuffix='"
        + fullSuffix
        + '\''
        + '}';
  }
}
