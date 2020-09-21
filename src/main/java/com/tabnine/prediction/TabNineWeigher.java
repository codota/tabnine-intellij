package com.tabnine.prediction;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;

public class TabNineWeigher extends LookupElementWeigher {
    public TabNineWeigher() {
        super("TabNineLookupElementWeigher", false, true);
    }

    @Override
    public Integer weigh(LookupElement element) {
        if (element instanceof TabNineLookupElement) {
            TabNineLookupElement tElement = (TabNineLookupElement) element;
            return tElement.index;
        }
        return Integer.MAX_VALUE;
    }
}
