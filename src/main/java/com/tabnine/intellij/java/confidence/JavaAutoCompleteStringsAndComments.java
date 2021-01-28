package com.tabnine.intellij.java.confidence;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.openapi.util.Key;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.util.ThreeState;
import com.tabnine.binary.requests.config.FeaturesRequest;
import com.tabnine.binary.requests.config.FeaturesResponse;
import com.tabnine.general.DependencyContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.tabnine.TabNineCompletionContributor.FORCE_AUTOPOPUP_KEY;

public class JavaAutoCompleteStringsAndComments extends CompletionConfidence {

    private boolean enabled = isEnabled();

    private boolean isEnabled() {
        final FeaturesResponse featuresResponse = DependencyContainer.instanceOfBinaryRequestFacade()
                .executeRequest(new FeaturesRequest());
        if (featuresResponse == null || featuresResponse.getEnabledFeatues() == null) {
            return false;
        }
        return Arrays.stream(featuresResponse.getEnabledFeatues())
                .anyMatch("completeCommentsAndStrings"::equals);
    }
    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        if (!enabled) {
            return ThreeState.UNSURE;
        }
        if (PsiJavaPatterns.psiElement()
                .inside(PsiJavaPatterns.or(
                        psiElement(PsiComment.class), psiElement(JavaTokenType.STRING_LITERAL)))
                .accepts(contextElement)) {
            contextElement.getParent().putCopyableUserData(FORCE_AUTOPOPUP_KEY, true);
            return ThreeState.NO;
        }
        return ThreeState.UNSURE;
    }
}
