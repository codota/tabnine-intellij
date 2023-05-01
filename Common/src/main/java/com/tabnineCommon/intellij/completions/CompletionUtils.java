package com.tabnineCommon.intellij.completions;

import static com.tabnineCommon.general.StaticConfig.MAX_COMPLETIONS;
import static com.tabnineCommon.general.Utils.endsWithADot;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.tabnineCommon.binary.requests.autocomplete.ResultEntry;
import com.tabnineCommon.general.SuggestionTrigger;
import com.tabnineCommon.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompletionUtils {

  @Nullable
  private static String getCursorPrefix(@NotNull Document document, int cursorPosition) {
    try {
      int lineNumber = document.getLineNumber(cursorPosition);
      int lineStart = document.getLineStartOffset(lineNumber);

      return document.getText(TextRange.create(lineStart, cursorPosition)).trim();
    } catch (Throwable e) {
      Logger.getInstance(CompletionUtils.class).warn("Failed to get cursor prefix: ", e);
      return null;
    }
  }

  @Nullable
  private static String getCursorSuffix(@NotNull Document document, int cursorPosition) {
    try {
      int lineNumber = document.getLineNumber(cursorPosition);
      int lineEnd = document.getLineEndOffset(lineNumber);

      return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    } catch (Throwable e) {
      Logger.getInstance(CompletionUtils.class).warn("Failed to get cursor suffix: ", e);
      return null;
    }
  }

  @Nullable
  public static TabNineCompletion createTabnineCompletion(
      @NotNull Document document,
      int offset,
      String oldPrefix,
      ResultEntry result,
      int index,
      SuggestionTrigger suggestionTrigger) {
    String cursorPrefix = CompletionUtils.getCursorPrefix(document, offset);
    String cursorSuffix = CompletionUtils.getCursorSuffix(document, offset);
    if (cursorPrefix == null || cursorSuffix == null) {
      return null;
    }

    return new TabNineCompletion(
        oldPrefix,
        result.new_prefix,
        result.old_suffix,
        result.new_suffix,
        index,
        cursorPrefix,
        cursorSuffix,
        result.completion_metadata,
        suggestionTrigger);
  }

  public static int completionLimit(
      CompletionParameters parameters, CompletionResultSet result, boolean isLocked) {
    return completionLimit(
        parameters.getEditor().getDocument(),
        result.getPrefixMatcher().getPrefix(),
        parameters.getOffset(),
        isLocked);
  }

  public static int completionLimit(
      @NotNull Document document, @NotNull String prefix, int offset, boolean isLocked) {
    if (isLocked) {
      return 1;
    }
    boolean preferTabNine = !endsWithADot(document, offset - prefix.length());

    return preferTabNine ? MAX_COMPLETIONS : 1;
  }
}
