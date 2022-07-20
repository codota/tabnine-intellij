package com.tabnine.general;

import static com.tabnine.general.StaticConfig.TABNINE_PLUGIN_ID;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Utils {
  private static final String UNKNOWN = "Unknown";

  public static String getTabNinePluginVersion() {
    return getTabNinePluginDescriptor().map(IdeaPluginDescriptor::getVersion).orElse(UNKNOWN);
  }

  @NotNull
  public static Optional<IdeaPluginDescriptor> getTabNinePluginDescriptor() {
    return Arrays.stream(PluginManager.getPlugins())
        .filter(plugin -> TABNINE_PLUGIN_ID.equals(plugin.getPluginId()))
        .findAny();
  }

  public static boolean endsWithADot(Document doc, int positionBeforeSuggestionPrefix) {
    int begin = positionBeforeSuggestionPrefix - ".".length();
    if (begin < 0 || positionBeforeSuggestionPrefix > doc.getTextLength()) {
      return false;
    } else {
      String tail = doc.getText(new TextRange(begin, positionBeforeSuggestionPrefix));
      return tail.equals(".");
    }
  }

  @NotNull
  public static String readContent(InputStream inputStream) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;

    while ((length = inputStream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }

    return result.toString(StandardCharsets.UTF_8.name()).trim();
  }

  @NotNull
  public static Integer toInt(@Nullable Long aLong) {
    if (aLong == null) {
      return 0;
    }

    return Math.toIntExact(aLong);
  }

  public static List<String> asLines(String block) {
    return Arrays.stream(block.split("\n")).collect(Collectors.toList());
  }

  public static String cmdSanitize(String text) {
    return text.replace(" ", "");
  }

  public static String wrapWithHtml(String content) {
    return wrapWithHtmlTag(content, "html");
  }

  public static String wrapWithHtmlTag(String content, String tag) {
    return "<" + tag + ">" + content + "</" + tag + ">";
  }

  public static long getDaysDiff(Date date1, Date date2) {
    try {
      return TimeUnit.DAYS.convert(
          Math.abs(date2.getTime() - date1.getTime()), TimeUnit.MILLISECONDS);
    } catch (NullPointerException e) {
      return -1;
    }
  }
}
