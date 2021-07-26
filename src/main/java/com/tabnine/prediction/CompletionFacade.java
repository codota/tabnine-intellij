package com.tabnine.prediction;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.exceptions.BinaryCannotRecoverException;
import com.tabnine.binary.requests.autocomplete.AutocompleteRequest;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.inline.InlineCompletionParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            String filename = ObjectUtils.doIfNotNull(parameters.getOriginalFile().getVirtualFile(), VirtualFile::getPath);
            return retrieveCompletions(parameters.getEditor().getDocument(), parameters.getOffset(), filename);
        } catch (BinaryCannotRecoverException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public AutocompleteResponse retrieveCompletions(@NotNull Document document, int offset) {
        try {
            String filename = ObjectUtils.doIfNotNull(FileDocumentManager.getInstance().getFile(document), VirtualFile::getPath);

            int begin = Integer.max(0, offset - MAX_OFFSET);
            int end = Integer.min(document.getTextLength(), offset + MAX_OFFSET);
            AutocompleteRequest req = new AutocompleteRequest();
            req.before = document.getText(new TextRange(begin, offset));
            req.after = document.getText(new TextRange(offset, end));
            req.filename = filename;
            req.maxResults = MAX_COMPLETIONS;
            req.regionIncludesBeginning = (begin == 0);
            req.regionIncludesEnd = (end == document.getTextLength());

            return binaryRequestFacade.executeRequest(req);
        } catch (BinaryCannotRecoverException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public AutocompleteResponse retrieveCompletions(@NotNull InlineCompletionParameters parameters) {
        try {
            int offset = parameters.getOffset();
            Document document = parameters.getDocument();
            String filename = ObjectUtils.doIfNotNull(FileDocumentManager.getInstance().getFile(document), VirtualFile::getPath);

            int begin = Integer.max(0, offset - MAX_OFFSET);
            int end = Integer.min(document.getTextLength(), offset + MAX_OFFSET);
            AutocompleteRequest req = new AutocompleteRequest();
            req.before = document.getText(new TextRange(begin, offset));
            req.after = document.getText(new TextRange(offset, end));
            req.filename = filename;
            req.maxResults = MAX_COMPLETIONS;
            req.regionIncludesBeginning = (begin == 0);
            req.regionIncludesEnd = (end == document.getTextLength());

            return binaryRequestFacade.executeRequest(req);
        } catch (BinaryCannotRecoverException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private AutocompleteResponse retrieveCompletions(@NotNull Document document, int offset, @Nullable String filename) throws Exception {
        return ApplicationUtil.runWithCheckCanceled(() -> {
            int middle = offset;
            int begin = Integer.max(0, middle - MAX_OFFSET);
            int end = Integer.min(document.getTextLength(), middle + MAX_OFFSET);
            AutocompleteRequest req = new AutocompleteRequest();
            req.before = document.getText(new TextRange(begin, middle));
            req.after = document.getText(new TextRange(middle, end));
            req.filename = filename;
            req.maxResults = MAX_COMPLETIONS;
            req.regionIncludesBeginning = (begin == 0);
            req.regionIncludesEnd = (end == document.getTextLength());

            return binaryRequestFacade.executeRequest(req);
        }, ProgressManager.getInstance().getProgressIndicator());
    }
}
