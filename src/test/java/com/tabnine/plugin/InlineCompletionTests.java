package com.tabnine.plugin;

import static com.intellij.openapi.actionSystem.IdeActions.*;
import static com.tabnine.plugin.InlineCompletionDriverKt.*;
import static com.tabnine.testUtils.TestData.THIRD_PREDICTION_RESULT;
import static org.mockito.Mockito.*;

import com.intellij.openapi.ide.CopyPasteManager;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.binary.requests.autocomplete.CompletionMetadata;
import com.tabnine.binary.requests.notifications.shown.SuggestionDroppedReason;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.inline.*;
import com.tabnine.testUtils.TestData;
import java.awt.datatransfer.StringSelection;
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

  private void mockCompletionResponse(String responce) throws Exception {
    when(binaryProcessGatewayMock.readRawResponse()).thenReturn(setOldPrefixFor(responce, "t"));
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
  public void shouldNotRemoveTheOldSuffixInMultiline() throws Exception {
    mockCompletionResponse(TestData.MULTI_LINE_SNIPPET_PREDICTION_RESULT);
    type("\nt");

    myFixture.performEditorAction(AcceptTabnineInlineCompletionAction.ACTION_ID);

    myFixture.checkResult("hello\ntemp\ntemp2\nhello");
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

  @Test
  public void skipSuggestionsOnPaste() throws Exception {
    mockCompletionResponseWithPrefix("tem");

    CopyPasteManager.getInstance().setContents(new StringSelection("tem"));
    myFixture.performEditorAction(ACTION_EDITOR_PASTE);

    assertNull(getTabnineCompletionContent(myFixture));
  }

  @Test
  public void skipSuggestionsOnTabKey() throws Exception {
    mockCompletionResponseWithPrefix("\t");

    type("\n");
    myFixture.performEditorAction(ACTION_EDITOR_TAB);

    assertNull(getTabnineCompletionContent(myFixture));
  }

  @Test
  public void nextSuggestionActionFiresEventsCorrectly() throws Exception {
    mockCompletionResponseWithPrefix("t");
    type("\nt");

    myFixture.performEditorAction(ShowNextTabnineInlineCompletionAction.ACTION_ID);

    verify(completionEventSenderMock, times(1))
        .sendToggleInlineSuggestionEvent(CompletionOrder.NEXT, 1);
  }

  @Test
  public void previousSuggestionActionFiresEventsCorrectly() throws Exception {
    mockCompletionResponseWithPrefix("t");
    type("\nt");

    myFixture.performEditorAction(ShowPreviousTabnineInlineCompletionAction.ACTION_ID);

    verify(completionEventSenderMock, times(1))
        .sendToggleInlineSuggestionEvent(CompletionOrder.PREVIOUS, 2);
  }

  @Test
  public void escapeSuggestionActionFiresEventCorrectly() throws Exception {
    mockCompletionResponseWithPrefix("t");
    type("\nt");

    myFixture.performEditorAction(EscapeHandler.ACTION_ID);

    verify(completionEventSenderMock, times(1))
        .sendSuggestionDropped(
            eq(3),
            anyString(),
            eq(SuggestionDroppedReason.ManualCancel),
            any(CompletionMetadata.class));
  }
}
