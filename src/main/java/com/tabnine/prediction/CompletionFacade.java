package com.tabnine.prediction;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.autocomplete.AutocompleteRequest;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;

import javax.annotation.Nullable;

import static com.tabnine.general.StaticConfig.MAX_COMPLETIONS;
import static com.tabnine.general.StaticConfig.MAX_OFFSET;

public class CompletionFacade {
    private final BinaryRequestFacade binaryRequestFacade;

    public CompletionFacade(BinaryRequestFacade binaryRequestFacade) {
        this.binaryRequestFacade = binaryRequestFacade;
    }

    @Nullable
    public AutocompleteResponse retrieveCompletions(CompletionParameters parameters) {
        try {
            return ApplicationUtil.runWithCheckCanceled(() -> {
                Document doc = parameters.getEditor().getDocument();
                int middle = parameters.getOffset();
                int begin = Integer.max(0, middle - MAX_OFFSET);
                int end = Integer.min(doc.getTextLength(), middle + MAX_OFFSET);
                AutocompleteRequest req = new AutocompleteRequest();
                req.before = doc.getText(new TextRange(begin, middle));
                req.after = doc.getText(new TextRange(middle, end));
                req.filename = parameters.getOriginalFile().getVirtualFile().getPath();
                req.maxResults = MAX_COMPLETIONS;
                req.regionIncludesBeginning = (begin == 0);
                req.regionIncludesEnd = (end == doc.getTextLength());

                return binaryRequestFacade.executeRequest(req);
            }, ProgressManager.getInstance().getProgressIndicator());
        } catch (Exception e) {
            return null;
        }
    }
}
