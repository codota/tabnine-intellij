package com.tabnine.inline;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CompletionState {
    private static final Key<CompletionState> INLINE_COMPLETION_STATE = Key.create("INLINE_COMPLETION_STATE");

    String prefix;
    int lastDisplayedCompletionIndex;
    int previousCallsCounter = 0;
    int nextCallsCounter = 0;
    boolean preInsertionHintShown;
    int lastStartOffset;
    long lastModCount;
    String lastDisplayedPreview;
    List<Integer> caretOffsets = Collections.emptyList();
    List<TabNineCompletion> suggestions;

    void resetStats(Editor editor) {
        this.nextCallsCounter = 0;
        this.previousCallsCounter = 0;
        this.lastModCount = 0;
    }

    public void suggestionBrowsed(boolean forward) {
        if (forward) {
            this.nextCallsCounter++;
        } else {
            this.previousCallsCounter++;
        }
    }

    public int getNextCallsCounter() {
        return nextCallsCounter;
    }

    public int getPreviousCallsCounter() {
        return previousCallsCounter;
    }

    public void preInsertionHintShown() {
        this.preInsertionHintShown = true;
    }

    public boolean isPreInsertionHintShown() {
        return this.preInsertionHintShown;
    }

    static CompletionState findOrCreateCompletionState(@NotNull Editor editor) {
        CompletionState state = editor.getUserData(INLINE_COMPLETION_STATE);
        if (state == null) {
            state = new CompletionState();
            editor.putUserData(INLINE_COMPLETION_STATE, state);
        }
        return state;
    }
}
