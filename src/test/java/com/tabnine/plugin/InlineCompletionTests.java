package com.tabnine.plugin;

import static com.tabnine.plugin.InlineCompletionDriverKt.*;
import static com.tabnine.testUtils.TestData.THIRD_PREDICTION_RESULT;
import static org.mockito.Mockito.when;

import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.inline.AcceptTabnineInlineCompletionAction;
import com.tabnine.inline.EscapeHandler;
import com.tabnine.inline.ShowNextTabnineInlineCompletionAction;
import com.tabnine.inline.ShowPreviousTabnineInlineCompletionAction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class InlineCompletionTests extends MockedBinaryCompletionTestCase {

  @Before
  public void init() {
    Mockito.when(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.INLINE);
  }

  private void mockCompletionResponseWithPrefix(String oldPrefix) throws Exception {
    when(binaryProcessGatewayMock.readRawResponse())
        .thenReturn(setOldPrefixFor(THIRD_PREDICTION_RESULT, oldPrefix));
  }

  @Test
  public void whenTextIsTypedThenShowInlineCompletion() throws Exception {
    mockCompletionResponseWithPrefix("t");

    type("\nt");

    assertEquals("Incorrect inline completion", "emp", getTabnineCompletionContent(myFixture));
  }

  @Test
  public void givenInlineCompletionIsShownWhenEscapedThenInlineShouldBeRemoved() throws Exception {
    mockCompletionResponseWithPrefix("t");

    type("\nt");
    myFixture.performEditorAction(EscapeHandler.ACTION_ID);

    assertNull(
        "Showing inline completion although escaped was triggered",
        getTabnineCompletionContent(myFixture));
  }

  @Test
  public void givenInlineCompletionIsShownWhenCursorIsMovedThenInlineShouldBeRemoved()
      throws Exception {
    mockCompletionResponseWithPrefix("t");

    type("\nt");
    myFixture.getEditor().getCaretModel().moveToOffset(0);

    assertNull(
        "Showing inline completion although caret was moved",
        getTabnineCompletionContent(myFixture));
  }

  @Test
  public void noInlineCompletionWhenAutocompleteSuggestionMode() throws Exception {
    Mockito.when(suggestionsModeServiceMock.getSuggestionMode())
        .thenReturn(SuggestionsMode.AUTOCOMPLETE);
    mockCompletionResponseWithPrefix("t");

    type("\nt");

    assertNull(
        "Should not show inline completion, but did", getTabnineCompletionContent(myFixture));
  }

  @Test
  public void showSecondSuggestionWhenExecutingNextInlineAction() throws Exception {
    mockCompletionResponseWithPrefix("te");

    type("\nte");
    myFixture.performEditorAction(ShowNextTabnineInlineCompletionAction.ACTION_ID);

    assertEquals("Incorrect inline completion", "mporary", getTabnineCompletionContent(myFixture));
  }

  @Test
  public void showLastSuggestionWhenExecutingPrevInlineAction() throws Exception {
    mockCompletionResponseWithPrefix("te");

    type("\nte");
    myFixture.performEditorAction(ShowPreviousTabnineInlineCompletionAction.ACTION_ID);

    assertEquals(
        "Incorrect inline completion", "mporary file", getTabnineCompletionContent(myFixture));
  }

  @Test
  public void showFirstSuggestionWhenExecutingNextAndThenPrevInlineActions() throws Exception {
    mockCompletionResponseWithPrefix("te");

    type("\nte");
    myFixture.performEditorAction(ShowNextTabnineInlineCompletionAction.ACTION_ID);
    myFixture.performEditorAction(ShowPreviousTabnineInlineCompletionAction.ACTION_ID);

    assertEquals("Incorrect inline completion", "mp", getTabnineCompletionContent(myFixture));
  }

  @Test
  public void acceptInlineCompletion() throws Exception {
    mockCompletionResponseWithPrefix("t");

    type("\nt");
    myFixture.performEditorAction(AcceptTabnineInlineCompletionAction.ACTION_ID);

    myFixture.checkResult("hello\ntemp\nhello");
  }

  @Test
  public void skipSuggestionsWhenMidlinePositionInvalid() throws Exception {
    mockCompletionResponseWithPrefix("t");

    myFixture.getEditor().getCaretModel().moveToOffset(0);
    type(" space");

    myFixture.getEditor().getCaretModel().moveToOffset(0);
    type("t");

    assertNull(getTabnineCompletionContent(myFixture));
  }
}
