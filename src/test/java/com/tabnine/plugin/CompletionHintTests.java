package com.tabnine.plugin;

import static com.tabnine.plugin.InlineCompletionDriverKt.*;
import static com.tabnine.testUtils.TestData.THIRD_PREDICTION_RESULT;
import static org.mockito.Mockito.when;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.balloon.FirstSuggestionHintTooltip;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.state.SuggestionHintState;
import com.tabnine.state.UserState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class CompletionHintTests extends MockedBinaryCompletionTestCase implements Disposable {
  private static MockedStatic<UserState> userStateStaticMock;

  private static final SuggestionHintState suggestionHintStateMock =
      Mockito.mock(SuggestionHintState.class);

  @Before
  public void init() {
    when(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.INLINE);
    userStateStaticMock = Mockito.mockStatic(UserState.class);

    ApplicationManager.setApplication(mockedApplicationWhichInvokesImmediately(), this);
  }

  @After
  public void clear() {
    userStateStaticMock.close();
    Disposer.dispose(this);
  }

  @Override
  public void dispose() {}

  private void mockCompletionResponseWithPrefix() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(setOldPrefixFor(THIRD_PREDICTION_RESULT, "t"));
  }

  private void mockIsEligibleForCompletionHint(boolean isEligible) {
    when(suggestionHintStateMock.isEligibleForSuggestionHint()).thenReturn(isEligible);
    UserState userStateMock = Mockito.mock(UserState.class);
    when(userStateMock.getSuggestionHintState()).thenReturn(suggestionHintStateMock);
    userStateStaticMock.when(UserState::getInstance).thenReturn(userStateMock);
  }

  @Test
  public void shouldShowCompletionHintWhenEligible() throws Exception {
    mockIsEligibleForCompletionHint(true);
    mockCompletionResponseWithPrefix();

    type("\nt");

    assertTrue(
        "first suggestion tooltip should be shown",
        FirstSuggestionHintTooltip.getSuggestionHintTooltip().isVisible());
    FirstSuggestionHintTooltip.getSuggestionHintTooltip().dispose();
  }

  @Test
  public void shouldNotShowCompletionHintWhenNotEligible() throws Exception {
    mockIsEligibleForCompletionHint(false);
    mockCompletionResponseWithPrefix();

    type("\nt");

    assertFalse(
        "first suggestion tooltip should not be shown",
        FirstSuggestionHintTooltip.getSuggestionHintTooltip().isVisible());
    FirstSuggestionHintTooltip.getSuggestionHintTooltip().dispose();
  }

  @Test
  public void shouldNotShowCompletionHintWhenNoCompletionsRetrieved() {
    mockIsEligibleForCompletionHint(true);

    type("\nt");

    assertFalse(
        "first suggestion tooltip should not be shown",
        FirstSuggestionHintTooltip.getSuggestionHintTooltip().isVisible());
    FirstSuggestionHintTooltip.getSuggestionHintTooltip().dispose();
  }
}
