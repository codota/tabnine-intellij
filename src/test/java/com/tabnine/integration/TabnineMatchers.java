package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class TabnineMatchers {
    public static Matcher<LookupElement> lookupElement(String suffix) {
        return new BaseMatcher<LookupElement>() {
            @Override
            public boolean matches(Object o) {
                LookupElement lookupElement = (LookupElement) o;

                return lookupElement.getLookupString().equals(suffix);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("LookupElement's string: ").appendValue(suffix);
            }
        };
    }

    public static Matcher<LookupElement> lookupBuilder(String hello) {
        return new BaseMatcher<LookupElement>() {
            @Override
            public boolean matches(Object o) {
                LookupElementBuilder lookupElement = (LookupElementBuilder) o;

                return lookupElement.getObject().equals(hello) && lookupElement.getLookupString().equals(hello);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("LookupElementBuilder's object and lookupString: ").appendValue(hello);
            }
        };
    }
}
