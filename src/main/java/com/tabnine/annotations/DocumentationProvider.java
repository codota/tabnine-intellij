package com.tabnine.annotations;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.FakePsiElement;
import com.tabnine.prediction.TabNineCompletion;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DocumentationProvider implements com.intellij.lang.documentation.DocumentationProvider {
    @Nullable
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        if (object instanceof TabNineCompletion) {
            return new MyFakeElement(element, (TabNineCompletion)object);
        }

        return com.intellij.lang.documentation.DocumentationProvider.super.getDocumentationElementForLookupItem(psiManager, object, element);
    }


    private class MyFakeElement extends FakePsiElement {
        private PsiElement parent;
        private TabNineCompletion completion;

        public MyFakeElement(PsiElement parent, TabNineCompletion completion) {
            this.parent = parent;
            this.completion = completion;
        }

        @Override
        public PsiElement getParent() {
            return parent;
        }
    }

    @Nullable
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof MyFakeElement) {
            TabNineCompletion completion = ((MyFakeElement)element).completion;

            if (completion.annotations != null) {
                String doc = Arrays.stream(completion.annotations)
                        .map(annotation -> String.format("%d: %s -> %s (%s)", annotation.offset, annotation.annotation, annotation.text, annotation.partial))
                        .collect(Collectors.joining("\n"));
                return StringEscapeUtils.escapeHtml(doc);
            }
        }
        return com.intellij.lang.documentation.DocumentationProvider.super.generateDoc(element, originalElement);
    }
}
