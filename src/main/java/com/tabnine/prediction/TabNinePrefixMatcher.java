package com.tabnine.prediction;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import org.jetbrains.annotations.NotNull;

public class TabNinePrefixMatcher extends PrefixMatcher {
    final PrefixMatcher inner;

    public TabNinePrefixMatcher(PrefixMatcher inner) {
        super(inner.getPrefix());
        this.inner = inner;
    }

    @Override
    public boolean prefixMatches(@NotNull LookupElement element) {
        if (element instanceof TabNineLookupElement) {
            return true;
        } else if (element instanceof LookupElementDecorator) {
            LookupElementDecorator decorator = (LookupElementDecorator) element;
            return prefixMatches(decorator.getDelegate());
        }
        return super.prefixMatches(element);
    }

    @Override
    public boolean isStartMatch(LookupElement element) {
        if (element instanceof TabNineLookupElement) {
            return true;
        }
        return super.isStartMatch(element);
    }

    @Override
    public boolean prefixMatches(@NotNull String name) {
        return this.inner.prefixMatches(name);
    }

    @Override
    public PrefixMatcher cloneWithPrefix(String prefix) {
        return new TabNinePrefixMatcher(this.inner.cloneWithPrefix(prefix));
    }
}
