package com.tabnine.prediction;

import static com.tabnine.general.StaticConfig.*;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.exceptions.BinaryCannotRecoverException;
import com.tabnine.binary.requests.autocomplete.AutocompleteRequest;
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.inline.render.GraphicsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompletionFacade {
  private final BinaryRequestFacade binaryRequestFacade;

  public CompletionFacade(BinaryRequestFacade binaryRequestFacade) {
    this.binaryRequestFacade = binaryRequestFacade;
  }

  @Nullable
  public AutocompleteResponse retrieveCompletions(CompletionParameters parameters, int tabSize) {
    try {
      String filename = getFilename(parameters.getOriginalFile().getVirtualFile());
      return ApplicationUtil.runWithCheckCanceled(
          () -> retrieveCompletions(parameters.getEditor(), parameters.getOffset(), filename, tabSize),
          ProgressManager.getInstance().getProgressIndicator());
    } catch (BinaryCannotRecoverException e) {
      throw e;
    } catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public AutocompleteResponse retrieveCompletions(@NotNull Editor editor, int offset, int tabSize) {
    try {
      String filename =
          getFilename(FileDocumentManager.getInstance().getFile(editor.getDocument()));
      return retrieveCompletions(editor, offset, filename, tabSize);
    } catch (BinaryCannotRecoverException e) {
      throw e;
    } catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public static String getFilename(@Nullable VirtualFile file) {
    return ObjectUtils.doIfNotNull(file, VirtualFile::getPath);
  }

  @Nullable
  private AutocompleteResponse retrieveCompletions(
      @NotNull Editor editor, int offset, @Nullable String filename, int tabSize) {
    Document document = editor.getDocument();

    int begin = Integer.max(0, offset - MAX_OFFSET);
    int end = Integer.min(document.getTextLength(), offset + MAX_OFFSET);
    AutocompleteRequest req = new AutocompleteRequest();
    req.before = document.getText(new TextRange(begin, offset));
    req.after = document.getText(new TextRange(offset, end));
    req.filename = filename;
    req.maxResults = MAX_COMPLETIONS;
    req.regionIncludesBeginning = (begin == 0);
    req.regionIncludesEnd = (end == document.getTextLength());
    req.offset = offset;
    req.line = document.getLineNumber(offset);
    req.character = offset - document.getLineStartOffset(req.line);
    req.indentation_size = tabSize;

    return binaryRequestFacade.executeRequest(req, determineTimeoutBy(req.before));
  }

  private int determineTimeoutBy(@NotNull String before) {
    if (SuggestionsMode.getSuggestionMode() != SuggestionsMode.INLINE) {
      return COMPLETION_TIME_THRESHOLD;
    }

    int lastNewline = before.lastIndexOf("\n");
    String lastLine = lastNewline >= 0 ? before.substring(lastNewline) : "";
    boolean endsWithWhitespacesOnly = lastLine.trim().isEmpty();
    return endsWithWhitespacesOnly ? NEWLINE_COMPLETION_TIME_THRESHOLD : COMPLETION_TIME_THRESHOLD;
  }
}
