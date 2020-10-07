package com.tabnine.testutils;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.tabnine.binary.fetch.BinaryVersion;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;

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

    public static <T> Matcher<List<T>> hasItemInPosition(int position, Matcher<T> value) {
        return new BaseMatcher<List<T>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("item in position ").appendValue(position).appendText(" ")
                        .appendDescriptionOf(value);
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof List)) {
                    return false;
                }

                return value.matches(((List) o).get(position));
            }
        };
    }

    public static Matcher<File> fileContentEquals(byte[] content) {
        return new BaseMatcher<File>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("file content is ").appendValue(content);
            }

            @Override
            public boolean matches(Object o) {
                try {
                    return Arrays.equals(Files.readAllBytes(((File) o).toPath()), content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static Path pathStartsWith(String s) {
        return argThat(argument -> argument.toString().startsWith(s));
    }

    public static <T> Matcher<Optional<T>> emptyOptional() {
        return Matchers.equalTo(Optional.empty());
    }

    @NotNull
    public static Matcher<Optional<BinaryVersion>> versionMatch(@Nonnull String version) {
        return new BaseMatcher<Optional<BinaryVersion>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("value ").appendValue(version);
            }

            @Override
            public boolean matches(Object o) {
                Optional<BinaryVersion> binaryVersions = (Optional<BinaryVersion>) o;

                return binaryVersions.get().getVersion().equals(version);
            }
        };
    }
}
