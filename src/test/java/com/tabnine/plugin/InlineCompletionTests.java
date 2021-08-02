package com.tabnine.plugin;

import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.inline.CompletionPreview;
import com.tabnine.inline.ShowNextInlineCompletionAction;
import com.tabnine.inline.ShowPreviousInlineCompletionAction;
import com.tabnine.inline.TabnineDocumentListener;
import com.tabnine.integration.MockedBinaryCompletionTestCase;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.tabnine.testutils.TestData.THIRD_PREDICTION_RESULT;
import static org.mockito.Mockito.when;

public class InlineCompletionTests extends MockedBinaryCompletionTestCase {

    private void configureInlineTest(SuggestionsMode suggestionsMode) throws Exception {
        when(binaryProcessGatewayMock.readRawResponse()).thenReturn(THIRD_PREDICTION_RESULT);
        MockedStatic<SuggestionsMode> suggestionsModeMock = Mockito.mockStatic(SuggestionsMode.class);
        suggestionsModeMock.when(SuggestionsMode::getSuggestionMode).thenReturn(suggestionsMode);
        myFixture.getEditor().getDocument().addDocumentListener(new TabnineDocumentListener());
    }

    @Test
    public void showInlineCompletion() throws Exception {
        configureInlineTest(SuggestionsMode.INLINE);

        type("\nt");
        assertEquals("Incorrect inline completion", "emp", CompletionPreview.getPreviewText(myFixture.getEditor()));
    }

    @Test
    public void noInlineCompletionWhenAutocompleteSuggestionMode() throws Exception {
        configureInlineTest(SuggestionsMode.AUTOCOMPLETE);

        type("\nt");
        assertNull(CompletionPreview.getPreviewText(myFixture.getEditor()));
    }

    @Test
    public void showFirstSuggestionWhenExecutingNextInlineAction() throws Exception {
        configureInlineTest(SuggestionsMode.INLINE);

        type("\nte");
        myFixture.performEditorAction(ShowNextInlineCompletionAction.ACTION_ID);
        assertEquals("Incorrect next inline completion", "mporary", CompletionPreview.getPreviewText(myFixture.getEditor()));
    }

    @Test
    public void showLastSuggestionWhenExecutingPrevInlineAction() throws Exception {
        configureInlineTest(SuggestionsMode.INLINE);

        type("\nte");
        myFixture.performEditorAction(ShowPreviousInlineCompletionAction.ACTION_ID);
        assertEquals("Incorrect previous inline completion", "mporary file", CompletionPreview.getPreviewText(myFixture.getEditor()));
    }
}
