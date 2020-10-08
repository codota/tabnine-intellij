package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.ide.plugins.PluginManagerCore;
import com.tabnine.general.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.tabnine.general.DependencyContainer.singletonOfTabNineDisablePluginListener;
import static com.tabnine.testutils.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SelectionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenTabNineCompletionWhenSelectedThenSetStateIsWrittenToBinary() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT, SET_STATE_RESPONSE);
        when(tabNineBinaryMock.getAndIncrementCorrelationId()).thenReturn(1);

        LookupElement[] lookupElements = myFixture.completeBasic();
        selectItem(lookupElements[1]);

        verify(tabNineBinaryMock).writeRequest(SET_STATE_REQUEST);
    }

    @Test
    public void givenTabNineCompletionWhenNotSelectedThenNotCounted() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT, SET_STATE_RESPONSE);
        when(tabNineBinaryMock.getAndIncrementCorrelationId()).thenReturn(1);

        myFixture.completeBasic();
        selectItem(new LookupElement() {
            @NotNull
            @Override
            public String getLookupString() {
                return "yay";
            }
        });

        // At most once because there is a completion request...
        verify(tabNineBinaryMock, atMostOnce()).writeRequest(any());
    }
}