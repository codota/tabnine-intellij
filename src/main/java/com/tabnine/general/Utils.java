package com.tabnine.general;

import static com.intellij.util.containers.ContainerUtil.removeDuplicates;
import static com.tabnine.general.StaticConfig.TABNINE_PLUGIN_ID;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.updateSettings.impl.UpdateSettings;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.concurrency.AppExecutorUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.Option;

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
    if (date1 != null && date2 != null) {
      return TimeUnit.DAYS.convert(
          Math.abs(date2.getTime() - date1.getTime()), TimeUnit.MILLISECONDS);
    }
    return -1;
  }

  public static long getHoursDiff(Date date1, Date date2) {
    if (date1 != null && date2 != null) {
      return TimeUnit.HOURS.convert(date2.getTime() - date1.getTime(), TimeUnit.MILLISECONDS);
    }
    return -1;
  }

  public static Future<?> executeUIThreadWithDelay(
      Runnable runnable, long delay, TimeUnit timeUnit) {
    return executeThread(
        () -> ApplicationManager.getApplication().invokeLater(runnable), delay, timeUnit);
  }

  public static Future<?> executeThread(Runnable runnable) {
    if (isUnitTestMode()) {
      runnable.run();
      return CompletableFuture.completedFuture(null);
    }
    return AppExecutorUtil.getAppExecutorService().submit(runnable);
  }

  public static Future<?> executeThread(Runnable runnable, long delay, TimeUnit timeUnit) {
    if (isUnitTestMode()) {
      runnable.run();
      return CompletableFuture.completedFuture(null);
    }
    return AppExecutorUtil.getAppScheduledExecutorService().schedule(runnable, delay, timeUnit);
  }

  public static boolean isUnitTestMode() {
    return ApplicationManager.getApplication() == null
        || ApplicationManager.getApplication().isUnitTestMode();
  }

  public static String trimEndSlashAndWhitespace(String text) {
    if (text == null) {
      return null;
    }
    return text.replaceAll("/\\s*$", "");
  }

  public static void SetCustomRepository(String cloud2Url) {
    if (!cloud2Url.trim().isEmpty()) {
      List<String> newPluginRepo = UpdateSettings.getInstance().getStoredPluginHosts();
      String newStore = String.format("%s/update/jetbrains/updatePlugins.xml", trimEndSlashAndWhitespace(cloud2Url));
      newPluginRepo.add(newStore);
      Logger.getInstance(Utils.class).debug(String.format("Added custom repository to %s", newStore));
      removeDuplicates(newPluginRepo);
    }
  }

  public static void RemoveCustomRepository(String oldCloud2Url) {
    if (!oldCloud2Url.trim().isEmpty()) {
      List<String> hostsList = UpdateSettings.getInstance().getStoredPluginHosts();
      String oldPluginRepo = String.format("%s/update/jetbrains/updatePlugins.xml", trimEndSlashAndWhitespace(oldCloud2Url));
      hostsList.remove(oldPluginRepo);
      Logger.getInstance(Utils.class).debug(String.format("Removed custom repository from %s", oldPluginRepo));
    }
  }
}
