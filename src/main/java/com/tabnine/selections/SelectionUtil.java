package com.tabnine.selections;

import static com.tabnine.general.Utils.toInt;
import static java.util.stream.Collectors.*;

import com.tabnine.binary.requests.selection.SelectionRequest;
import com.tabnine.binary.requests.selection.SelectionSuggestionRequest;
import com.tabnine.general.CompletionOrigin;
import com.tabnine.prediction.TabNineCompletion;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class SelectionUtil {

  public static void addSuggestionsCount(
      SelectionRequest selection, List<TabNineCompletion> suggestions) {
    Map<CompletionOrigin, Long> originCount =
        suggestions.stream().collect(groupingBy(TabNineCompletion::getOrigin, counting()));

    selection.suggestionsCount = suggestions.size();
    selection.deepCloudSuggestionsCount =
        toInt(originCount.get(CompletionOrigin.CLOUD))
            + toInt(originCount.get(CompletionOrigin.CLOUD2))
            + toInt(originCount.get(CompletionOrigin.ANBU));
    selection.deepLocalSuggestionsCount = toInt(originCount.get(CompletionOrigin.LOCAL));
    selection.lspSuggestionsCount = toInt(originCount.get(CompletionOrigin.LSP));
    selection.vanillaSuggestionsCount = toInt(originCount.get(CompletionOrigin.VANILLA));

    selection.suggestions =
        suggestions.stream()
            .map(
                suggestion ->
                    new SelectionSuggestionRequest(
                        suggestion.newPrefix.length(),
                        getStrength(suggestion),
                        suggestion.origin.name()))
            .collect(toList());
  }

  public static String getStrength(TabNineCompletion item) {
    if (item.origin == CompletionOrigin.LSP) {
      return null;
    }

    return item.detail;
  }

  @NotNull
  static String asLanguage(String name) {
    String[] split = name.split("\\.");

    return Arrays.stream(split).skip(Math.max(1, split.length - 1)).findAny().orElse("undefined");
  }
}
