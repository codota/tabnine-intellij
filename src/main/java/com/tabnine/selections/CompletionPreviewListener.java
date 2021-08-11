package com.tabnine.selections;

import com.intellij.psi.PsiFile;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.selection.SelectionRequest;
import com.tabnine.binary.requests.selection.SetStateBinaryRequest;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.statusBar.StatusBarUpdater;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompletionPreviewListener {
    private final BinaryRequestFacade binaryRequestFacade;
    private final StatusBarUpdater statusBarUpdater;

    public CompletionPreviewListener(BinaryRequestFacade binaryRequestFacade,
                                 StatusBarUpdater statusBarUpdater) {
        this.binaryRequestFacade = binaryRequestFacade;
        this.statusBarUpdater = statusBarUpdater;
    }

    public void previewSelected(CompletionPreviewData data) {
        TabNineCompletion completion = data.completions.get(data.previewIndex);
        SelectionRequest selection = new SelectionRequest();

        selection.language = SelectionUtil.asLanguage(data.file.getName());
        selection.netLength = completion.newPrefix.replaceFirst("^" + completion.completionPrefix, "").length();
        selection.linePrefixLength = completion.cursorPrefix.length();
        selection.lineNetPrefixLength = selection.linePrefixLength - completion.completionPrefix.length();
        selection.lineSuffixLength = completion.cursorSuffix.length();
        selection.index = data.previewIndex;
        selection.origin = completion.origin;
        selection.length = completion.newPrefix.length();
        selection.strength = SelectionUtil.getStrength(completion);
        SelectionUtil.addSuggestionsCount(selection, data.completions);

        binaryRequestFacade.executeRequest(new SetStateBinaryRequest(selection));
        this.statusBarUpdater.updateStatusBar();
    }

    public static class CompletionPreviewData {
        List<TabNineCompletion> completions;
        int previewIndex;
        @NotNull PsiFile file;

        public CompletionPreviewData(List<TabNineCompletion> completions, int previewIndex, @NotNull PsiFile file) {
            this.completions = completions;
            this.previewIndex = previewIndex;
            this.file = file;
        }
    }
}
