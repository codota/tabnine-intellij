package com.tabnineCommon.plugin;

import static org.mockito.Mockito.when;

import com.tabnineCommon.MockedBinaryCompletionTestCase;
import com.tabnineCommon.balloon.FirstSuggestionHintTooltip;
import com.tabnineCommon.capabilities.CapabilitiesService;
import com.tabnineCommon.capabilities.Capability;
import com.tabnineCommon.capabilities.SuggestionsMode;
import com.tabnineCommon.state.SuggestionHintState;
import com.tabnineCommon.state.UserState;
import com.tabnineCommon.testUtils.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class CompletionHintTests extends MockedBinaryCompletionTestCase {
  private static MockedStatic<UserState> userStateStaticMock;

  private static final SuggestionHintState suggestionHintStateMock =
      Mockito.mock(SuggestionHintState.class);

  private static final CapabilitiesService capabilityServiceMock =
      Mockito.mock(CapabilitiesService.class);

  @Before
  public void init() {
    when(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.INLINE);
    userStateStaticMock = Mockito.mockStatic(UserState.class);
  }

  @After
  public void clear() {
    userStateStaticMock.close();
  }

  private void mockCompletionResponseWithPrefix() throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(
            InlineCompletionDriverKt.setOldPrefixFor(TestData.THIRD_PREDICTION_RESULT, "t"));
  }

  private void mockIsEligibleForCompletionHint(boolean isEligible) {
    when(capabilityServiceMock.isCapabilityEnabled(Capability.FIRST_SUGGESTION_HINT_ENABLED))
        .thenReturn(true);
    when(CapabilitiesService.getInstance()).thenReturn(capabilityServiceMock);
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
