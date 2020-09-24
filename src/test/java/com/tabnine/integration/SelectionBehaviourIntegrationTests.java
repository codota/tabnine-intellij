package com.tabnine.integration;

import com.intellij.codeInsight.completion.LightFixtureCompletionTestCase;
import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnine.binary.TabNineBinary;
import com.tabnine.binary.TabNineProcessFacade;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.tabnine.integration.TabnineMatchers.lookupBuilder;
import static com.tabnine.integration.TabnineMatchers.lookupElement;
import static com.tabnine.integration.TestData.*;
import static com.tabnine.integration.TestData.SOME_CONTENT;
import static org.hamcrest.Matchers.array;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SelectionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
    @Test
    public void givenAFileWhenCompletionFiredThenRequestIsWrittenToBinaryProcessInputCorrectly() throws Exception {
        when(tabNineBinaryMock.readRawResponse()).thenReturn(A_PREDICTION_RESULT);
        when(tabNineBinaryMock.getAndIncrementCorrelationId()).thenReturn(1);

        LookupElement[] lookupElements = myFixture.completeBasic();
        selectItem(lookupElements[1]);
    }
}