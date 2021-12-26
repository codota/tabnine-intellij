package com.tabnine.inline;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompletionState {
    private static final Key<CompletionState> INLINE_COMPLETION_STATE =
            Key.create("INLINE_COMPLETION_STATE");

    String prefix;
    int lastDisplayedCompletionIndex;
    boolean preInsertionHintShown;
    int lastStartOffset;
    long lastModificationStamp;
    String lastDisplayedPreview;
    List<TabNineCompletion> suggestions;

    void resetStats() {
        this.lastModificationStamp = 0;
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

    static void clearCompletionState(@NotNull Editor editor) {
        editor.putUserData(INLINE_COMPLETION_STATE, null);
    }
}
