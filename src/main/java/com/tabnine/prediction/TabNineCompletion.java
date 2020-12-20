package com.tabnine.prediction;

import com.tabnine.general.CompletionOrigin;

public class TabNineCompletion {
    public final String oldPrefix;
    public final String newPrefix;
    public final String oldSuffix;
    public final String newSuffix;
    public final int index;
    public String completionPrefix;
    public String cursorPrefix;
    public String cursorSuffix;
    public CompletionOrigin origin;

    public String detail = null;
    public boolean deprecated = false;

    public TabNineCompletion(String oldPrefix, String newPrefix, String oldSuffix, String newSuffix, int index, String completionPrefix, String cursorPrefix, String cursorSuffix, CompletionOrigin origin) {
        this.oldPrefix = oldPrefix;
        this.newPrefix = newPrefix;
        this.oldSuffix = oldSuffix;
        this.newSuffix = newSuffix;
        this.index = index;
        this.completionPrefix = completionPrefix;
        this.cursorPrefix = cursorPrefix;
        this.cursorSuffix = cursorSuffix;
        this.origin = origin;
    }

    public CompletionOrigin getOrigin() {
        return origin;
    }
}
