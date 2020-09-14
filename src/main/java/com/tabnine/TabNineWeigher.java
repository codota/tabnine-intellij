package com.tabnine;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;

class TabNineWeigher extends LookupElementWeigher {
    TabNineWeigher() {
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
