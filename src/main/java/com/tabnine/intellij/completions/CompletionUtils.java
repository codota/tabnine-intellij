package com.tabnine.intellij.completions;

import static com.tabnine.general.StaticConfig.MAX_COMPLETIONS;
import static com.tabnine.general.Utils.endsWithADot;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.tabnine.binary.requests.autocomplete.ResultEntry;
import com.tabnine.general.SuggestionTrigger;
import com.tabnine.prediction.TabNineCompletion;
import java.util.Map;
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
      Map<String, Object> snippetContext,
      SuggestionTrigger suggestionTrigger) {
    String cursorPrefix = CompletionUtils.getCursorPrefix(document, offset);
    String cursorSuffix = CompletionUtils.getCursorSuffix(document, offset);
    if (cursorPrefix == null || cursorSuffix == null) {
      return null;
    }

    TabNineCompletion completion =
        new TabNineCompletion(
            oldPrefix,
            result.new_prefix,
            result.old_suffix,
            result.new_suffix,
            index,
            cursorPrefix,
            cursorSuffix,
            result.origin,
            result.completion_kind,
            result.is_cached,
            snippetContext,
            suggestionTrigger);

    completion.detail = result.detail;

    if (result.deprecated != null) {
      completion.deprecated = result.deprecated;
    }

    return completion;
  }

  static int completionLimit(
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
