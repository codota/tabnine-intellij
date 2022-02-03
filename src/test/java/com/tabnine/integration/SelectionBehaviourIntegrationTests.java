package com.tabnine.integration;

import static com.tabnine.testUtils.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class SelectionBehaviourIntegrationTests extends MockedBinaryCompletionTestCase {
  @Test
  public void givenTabNineCompletionWhenSelectedThenSetStateIsWrittenToBinary() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(A_PREDICTION_RESULT, SET_STATE_RESPONSE);

    LookupElement[] lookupElements = myFixture.completeBasic();
    selectItem(lookupElements[1]);

    verify(binaryProcessGatewayMock).writeRequest(SET_STATE_REQUEST);
  }

  @Test
  public void givenAFileWithNoExtensionWhenSelectedThenSetStateExtensionIsUndefined()
      throws Exception {
    myFixture.configureByText(A_FILE_WITH_NO_EXTENSION, SOME_CONTENT);
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(A_PREDICTION_RESULT, SET_STATE_RESPONSE);

    LookupElement[] lookupElements = myFixture.completeBasic();
    selectItem(lookupElements[1]);

    verify(binaryProcessGatewayMock).writeRequest(NO_EXTENSION_STATE_REQUEST);
  }

  @Test
  public void givenTabNineCompletionWhenNotSelectedThenNotCounted() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(A_PREDICTION_RESULT, SET_STATE_RESPONSE);

    LookupElement[] lookupElements = myFixture.completeBasic();
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
