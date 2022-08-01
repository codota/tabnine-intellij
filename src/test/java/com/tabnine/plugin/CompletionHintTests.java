package com.tabnine.plugin;

import static com.tabnine.plugin.InlineCompletionDriverKt.*;
import static com.tabnine.testUtils.TestData.THIRD_PREDICTION_RESULT;
import static org.mockito.Mockito.when;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.tabnine.balloon.FirstSuggestionHintTooltip;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.integration.MockedBinaryCompletionTestCase;
import com.tabnine.state.SuggestionHintState;
import com.tabnine.state.UserState;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class CompletionHintTests extends MockedBinaryCompletionTestCase implements Disposable {
  private static MockedStatic<SuggestionsMode> suggestionsModeMock;
  private static MockedStatic<UserState> userStateStaticMock;

  @Before
  public void init() {
    suggestionsModeMock = Mockito.mockStatic(SuggestionsMode.class);
    suggestionsModeMock.when(SuggestionsMode::getSuggestionMode).thenReturn(SuggestionsMode.INLINE);

    userStateStaticMock = Mockito.mockStatic(UserState.class);

    ApplicationManager.setApplication(mockedApplicationWhichInvokesImmediately(), this);
  }

  @After
  public void clear() {
    suggestionsModeMock.close();
    userStateStaticMock.close();
    Disposer.dispose(this);
  }

  @Override
  public void dispose() {}

  private void mockCompletionResponseWithPrefix(String oldPrefix) throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(setOldPrefixFor(THIRD_PREDICTION_RESULT, oldPrefix));
  }

  private void mockIsEligibleForCompletionHint(boolean isEligible) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date installationTime = isEligible ? new Date() : new Date(0);
    SuggestionHintState suggestionHintState =
        new SuggestionHintState(dateFormat.format(installationTime));
    UserState userStateMock = Mockito.mock(UserState.class);
    when(userStateMock.getSuggestionHintState()).thenReturn(suggestionHintState);
    userStateStaticMock.when(UserState::getInstance).thenReturn(userStateMock);
  }

  @Test
  public void shouldShowCompletionHintWhenEligible() throws Exception {
    mockIsEligibleForCompletionHint(true);
    mockCompletionResponseWithPrefix("t");

    type("\nt");

    assertTrue(
        "first suggestion tooltip should be shown",
        FirstSuggestionHintTooltip.getSuggestionHintTooltip().isVisible());
    FirstSuggestionHintTooltip.getSuggestionHintTooltip().dispose();
  }

  @Test
  public void shouldNotShowCompletionHintWhenNotEligible() throws Exception {
    mockIsEligibleForCompletionHint(false);
    mockCompletionResponseWithPrefix("t");

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
