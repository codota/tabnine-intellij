package com.tabnine.plugin;

import static com.tabnine.plugin.InlineCompletionDriverKt.*;
import static com.tabnine.testUtils.TestData.THIRD_PREDICTION_RESULT;
import static org.mockito.Mockito.when;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.inline.AcceptInlineCompletionAction;
import com.tabnine.inline.EscapeHandler;
import com.tabnine.inline.ShowNextInlineCompletionAction;
import com.tabnine.inline.ShowPreviousInlineCompletionAction;
import com.tabnine.MockedBinaryCompletionTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class InlineCompletionTests extends MockedBinaryCompletionTestCase implements Disposable {

  @Before
  public void init() {
    Mockito.when(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.INLINE);
    ApplicationManager.setApplication(mockedApplicationWhichInvokesImmediately(), this);
  }

  @After
  public void clear() {
    Disposer.dispose(this);
  }

  @Override
  public void dispose() {}

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
    myFixture.performEditorAction(ShowNextInlineCompletionAction.ACTION_ID);

    assertEquals("Incorrect inline completion", "mporary", getTabnineCompletionContent(myFixture));
  }

  @Test
  public void showLastSuggestionWhenExecutingPrevInlineAction() throws Exception {
    mockCompletionResponseWithPrefix("te");

    type("\nte");
    myFixture.performEditorAction(ShowPreviousInlineCompletionAction.ACTION_ID);

    assertEquals(
        "Incorrect inline completion", "mporary file", getTabnineCompletionContent(myFixture));
  }

  @Test
  public void showFirstSuggestionWhenExecutingNextAndThenPrevInlineActions() throws Exception {
    mockCompletionResponseWithPrefix("te");

    type("\nte");
    myFixture.performEditorAction(ShowNextInlineCompletionAction.ACTION_ID);
    myFixture.performEditorAction(ShowPreviousInlineCompletionAction.ACTION_ID);

    assertEquals("Incorrect inline completion", "mp", getTabnineCompletionContent(myFixture));
  }

  @Test
  public void acceptInlineCompletion() throws Exception {
    mockCompletionResponseWithPrefix("t");

    type("\nt");
    myFixture.performEditorAction(AcceptInlineCompletionAction.ACTION_ID);

    myFixture.checkResult("hello\ntemp\nhello");
  }
}
