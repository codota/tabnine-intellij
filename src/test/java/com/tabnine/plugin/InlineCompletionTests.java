package com.tabnine.plugin;

import static com.tabnine.testUtils.TestData.THIRD_PREDICTION_RESULT;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.util.ObjectUtils;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.inline.*;
import com.tabnine.integration.MockedBinaryCompletionTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class InlineCompletionTests extends MockedBinaryCompletionTestCase {
  private static final ObjectMapper mapper = createObjectMapper();
  private static MockedStatic<SuggestionsMode> suggestionsModeMock;

  @Before
  public void init() {
    suggestionsModeMock = Mockito.mockStatic(SuggestionsMode.class);
  }

  @After
  public void clear() {
    suggestionsModeMock.close();
  }

  private void configureInlineTest(SuggestionsMode suggestionsMode, String oldPrefix)
      throws Exception {
    String value = setOldPrefixFor(THIRD_PREDICTION_RESULT, oldPrefix);
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(value);
    suggestionsModeMock.when(SuggestionsMode::getSuggestionMode).thenReturn(suggestionsMode);
  }

  private String setOldPrefixFor(String completionMock, String oldPrefix)
      throws JsonProcessingException {
    AutocompleteResponse autocompleteResponse =
        mapper.readValue(completionMock, AutocompleteResponse.class);
    autocompleteResponse.old_prefix = oldPrefix;
    return mapper.writeValueAsString(autocompleteResponse);
  }

  private static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    // it fails on `docs` fields which is not presented in `AutocompleteResponse`, but that's
    // (serialization works in production).
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper;
  }

  @Test
  public void showInlineCompletion() throws Exception {
    configureInlineTest(SuggestionsMode.INLINE, "t");

    type("\nt");
    assertEquals(
        "Incorrect inline completion",
        "emp",
        CompletionState.getOrCreateInstance(myFixture.getEditor())
            .getCurrentCompletion()
            .getSuffix());
  }

  @Test
  public void noInlineCompletionWhenAutocompleteSuggestionMode() throws Exception {
    configureInlineTest(SuggestionsMode.AUTOCOMPLETE, "t");

    type("\nt");
    assertNull(
        CompletionState.getOrCreateInstance(myFixture.getEditor())
            .getCurrentCompletion()
            .getSuffix());
  }

  @Test
  public void showSecondSuggestionWhenExecutingNextInlineAction() throws Exception {
    configureInlineTest(SuggestionsMode.INLINE, "te");

    type("\nte");
    myFixture.performEditorAction(ShowNextInlineCompletionAction.ACTION_ID);
    assertEquals(
        "Incorrect next inline completion",
        "mporary",
        CompletionState.getOrCreateInstance(myFixture.getEditor())
            .getCurrentCompletion()
            .getSuffix());
  }

  @Test
  public void showLastSuggestionWhenExecutingPrevInlineAction() throws Exception {
    configureInlineTest(SuggestionsMode.INLINE, "te");

    type("\nte");
    myFixture.performEditorAction(ShowPreviousInlineCompletionAction.ACTION_ID);
    assertEquals(
        "Incorrect previous inline completion",
        "mporary file",
        CompletionState.getOrCreateInstance(myFixture.getEditor())
            .getCurrentCompletion()
            .getSuffix());
  }

  @Test
  public void showFirstSuggestionWhenExecutingNextAndThenPrevInlineActions() throws Exception {
    configureInlineTest(SuggestionsMode.INLINE, "te");

    type("\nte");
    myFixture.performEditorAction(ShowNextInlineCompletionAction.ACTION_ID);
    myFixture.performEditorAction(ShowPreviousInlineCompletionAction.ACTION_ID);
    assertEquals(
        "Incorrect next inline completion",
        "mp",
        CompletionState.getOrCreateInstance(myFixture.getEditor())
            .getCurrentCompletion()
            .getSuffix());
  }

  @Test
  public void acceptInlineCompletion() throws Exception {
    configureInlineTest(SuggestionsMode.INLINE, "t");

    type("\nt");
    myFixture.performEditorAction(AcceptInlineCompletionAction.ACTION_ID);
    myFixture.checkResult("hello\ntemp\nhello");
  }

  @Test
  public void dontShowPreviewForAutoFillingChars() throws Exception {
    configureInlineTest(SuggestionsMode.INLINE, "[");

    type("\n");
    type("[");
    // only test the auto-filled char, so dispose the preview before the last character
    ObjectUtils.doIfNotNull(
        myFixture.getEditor(),
        editor -> {
          CompletionPreview.clear(editor);
          return null;
        });

    type("]");
    assertNull(
        "Should not have shown preview",
        CompletionState.getOrCreateInstance(myFixture.getEditor())
            .getCurrentCompletion()
            .getSuffix());
  }
}
