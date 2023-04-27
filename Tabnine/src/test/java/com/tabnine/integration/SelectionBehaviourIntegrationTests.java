package com.tabnine.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.intellij.codeInsight.lookup.LookupElement;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.testUtils.TestData;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class SelectionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
  @Test
  public void givenTabNineCompletionWhenSelectedThenSetStateIsWrittenToBinary() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(TestData.A_PREDICTION_RESULT, TestData.SET_STATE_RESPONSE);

    LookupElement[] lookupElements = myFixture.completeBasic();
    selectItem(lookupElements[1]);

    verify(binaryProcessGatewayMock).writeRequest(TestData.SET_STATE_REQUEST);
  }

  @Test
  public void givenAFileWithNoExtensionWhenSelectedThenSetStateExtensionIsUndefined()
      throws Exception {
    myFixture.configureByText(TestData.A_FILE_WITH_NO_EXTENSION, TestData.SOME_CONTENT);
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(TestData.A_PREDICTION_RESULT, TestData.SET_STATE_RESPONSE);

    LookupElement[] lookupElements = myFixture.completeBasic();
    selectItem(lookupElements[1]);

    verify(binaryProcessGatewayMock).writeRequest(TestData.NO_EXTENSION_STATE_REQUEST);
  }

  @Test
  public void givenTabNineCompletionWhenNotSelectedThenNotCounted() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(TestData.A_PREDICTION_RESULT, TestData.SET_STATE_RESPONSE);

    myFixture.completeBasic();

    selectItem(
        new LookupElement() {
          @NotNull
          @Override
          public String getLookupString() {
            return "yay";
          }
        });

    // At most once because there is a completion request...
    verify(binaryProcessGatewayMock, atMostOnce()).writeRequest(any());
  }
}
