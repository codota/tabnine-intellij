package com.tabnine.prediction;

import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.FList;
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

    public String detail = null;
    public boolean deprecated = false;

    public TabNineCompletion(String oldPrefix, String newPrefix, String oldSuffix, String newSuffix, int index, String cursorPrefix, String cursorSuffix, CompletionOrigin origin, CompletionKind completionKind, Boolean isCached) {
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
    }

    public CompletionOrigin getOrigin() {
        return origin;
    }

    public String getSuffix() {
        String itemText = this.newPrefix + this.newSuffix;
        String prefix = this.oldPrefix;
        if (prefix.isEmpty()) {
            return itemText;
        }

        FList<TextRange> fragments = LookupCellRenderer.getMatchingFragments(prefix, itemText);
        if (fragments != null && !fragments.isEmpty()) {
            List<TextRange> list = new ArrayList<>(fragments);
            return itemText.substring(list.get(list.size() - 1).getEndOffset());
        }
        return "";
    }
}
