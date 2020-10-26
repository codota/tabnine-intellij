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
        if (element.getObject() instanceof TabNineCompletion) {
            return true;
        } else if (element instanceof LookupElementDecorator) {
            return prefixMatches(((LookupElementDecorator) element).getDelegate());
        }

        return super.prefixMatches(element);
    }

    @Override
    public boolean isStartMatch(LookupElement element) {
        if (element.getObject() instanceof TabNineCompletion) {
            return true;
        }

        return super.isStartMatch(element);
    }

    @Override
    public boolean prefixMatches(@NotNull String name) {
        return this.inner.prefixMatches(name);
    }

    @NotNull
    @Override
    public PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
        return new TabNinePrefixMatcher(this.inner.cloneWithPrefix(prefix));
    }
}
