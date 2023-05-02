package com.tabnine.intellij.completions;

import static com.tabnineCommon.general.StaticConfig.*;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.messages.MessageBus;
import com.tabnine.selections.TabNineLookupListener;
import com.tabnine.statusBar.StatusBarUpdater;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.autocomplete.AutocompleteResponse;
import com.tabnineCommon.binary.requests.autocomplete.ResultEntry;
import com.tabnineCommon.capabilities.RenderingMode;
import com.tabnineCommon.capabilities.SuggestionsMode;
import com.tabnineCommon.capabilities.SuggestionsModeService;
import com.tabnineCommon.config.Config;
import com.tabnineCommon.general.CompletionsEventSender;
import com.tabnineCommon.general.DependencyContainer;
import com.tabnineCommon.general.EditorUtils;
import com.tabnineCommon.general.StaticConfig;
import com.tabnineCommon.inline.TabnineInlineLookupListener;
import com.tabnineCommon.inline.render.GraphicsUtilsKt;
import com.tabnineCommon.intellij.completions.Completion;
import com.tabnineCommon.intellij.completions.CompletionUtils;
import com.tabnineCommon.prediction.CompletionFacade;
import com.tabnineCommon.prediction.TabNineCompletion;
import com.tabnineCommon.prediction.TabNinePrefixMatcher;
import com.tabnineCommon.prediction.TabNineWeigher;
import com.tabnineCommon.selections.AutoImporter;
import com.tabnineCommon.userSettings.AppSettingsState;
import java.util.ArrayList;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabNineCompletionContributor extends CompletionContributor {
  private final CompletionFacade completionFacade =
      DependencyContainer.instanceOfCompletionFacade();
  private final TabNineLookupListener tabNineLookupListener = instanceOfTabNineLookupListener();
  private final TabnineInlineLookupListener tabNineInlineLookupListener =
      DependencyContainer.instanceOfTabNineInlineLookupListener();
  private final SuggestionsModeService suggestionsModeService =
      DependencyContainer.instanceOfSuggestionsModeService();
  private final CompletionsEventSender completionsEventSender =
      DependencyContainer.instanceOfCompletionsEventSender();
  private final MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
  private boolean isLocked;

  public static synchronized TabNineLookupListener instanceOfTabNineLookupListener() {
    final BinaryRequestFacade binaryRequestFacade =
        DependencyContainer.instanceOfBinaryRequestFacade();
    return new TabNineLookupListener(
        binaryRequestFacade, new StatusBarUpdater(binaryRequestFacade));
  }

  @Override
  public void fillCompletionVariants(
      @NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet) {
    if (!EditorUtils.isMainEditor(parameters.getEditor())) {
      return;
    }

    if (!parameters.isAutoPopup()) {
      completionsEventSender.sendManualSuggestionTrigger(RenderingMode.AUTOCOMPLETE);
    }

    if (suggestionsModeService.getSuggestionMode().isInlineEnabled()) {
      registerLookupListener(parameters, tabNineInlineLookupListener);
    }
    if (!suggestionsModeService.getSuggestionMode().isPopupEnabled()) {
      return;
    }
    registerLookupListener(parameters, tabNineLookupListener);
    AutocompleteResponse completions =
        this.completionFacade.retrieveCompletions(
            parameters, GraphicsUtilsKt.getTabSize(parameters.getEditor()));

    if (completions == null) {
      return;
    }

    PrefixMatcher originalMatcher = resultSet.getPrefixMatcher();

    if (originalMatcher.getPrefix().length() == 0 && completions.results.length == 0) {
      return;
    }

    if (suggestionsModeService.getSuggestionMode() == SuggestionsMode.HYBRID
        && Arrays.stream(completions.results).anyMatch(Completion::isSnippet)) {
      return;
    }

    if (this.isLocked != completions.is_locked) {
      this.isLocked = completions.is_locked;
      this.messageBus
          .syncPublisher(LimitedSecletionsChangedNotifier.LIMITED_SELECTIONS_CHANGED_TOPIC)
          .limitedChanged(completions.is_locked);
    }

    resultSet =
        resultSet
            .withPrefixMatcher(
                new TabNinePrefixMatcher(originalMatcher.cloneWithPrefix(completions.old_prefix)))
            .withRelevanceSorter(
                CompletionSorter.defaultSorter(parameters, originalMatcher)
                    .weigh(new TabNineWeigher()));
    resultSet.restartCompletionOnAnyPrefixChange();

    addAdvertisement(resultSet, completions);

    resultSet.addAllElements(createCompletions(completions, parameters, resultSet));
  }

  private ArrayList<LookupElement> createCompletions(
      AutocompleteResponse completions,
      @NotNull CompletionParameters parameters,
      @NotNull CompletionResultSet resultSet) {
    ArrayList<LookupElement> elements = new ArrayList<>();
    final Lookup activeLookup = LookupManager.getActiveLookup(parameters.getEditor());
    for (int index = 0;
        index < completions.results.length
            && index
                < CompletionUtils.completionLimit(parameters, resultSet, completions.is_locked);
        index++) {
      LookupElement lookupElement =
          createCompletion(
              parameters,
              completions.old_prefix,
              completions.results[index],
              index,
              completions.is_locked,
              activeLookup);

      if (lookupElement != null && resultSet.getPrefixMatcher().prefixMatches(lookupElement)) {
        elements.add(lookupElement);
      }
    }

    return elements;
  }

  @Nullable
  private LookupElement createCompletion(
      CompletionParameters parameters,
      String oldPrefix,
      ResultEntry result,
      int index,
      boolean locked,
      @Nullable Lookup activeLookup) {
    TabNineCompletion completion =
        CompletionUtils.createTabnineCompletion(
            parameters.getEditor().getDocument(),
            parameters.getOffset(),
            oldPrefix,
            result,
            index,
            null);
    if (completion == null) {
      return null;
    }

    LookupElementBuilder lookupElementBuilder =
        LookupElementBuilder.create(completion, result.new_prefix)
            .withRenderer(
                new LookupElementRenderer<LookupElement>() {
                  @Override
                  public void renderElement(
                      LookupElement element, LookupElementPresentation presentation) {
                    TabNineCompletion lookupElement = (TabNineCompletion) element.getObject();
                    String typeText = (locked ? LIMITATION_SYMBOL : "");
                    if (Config.DISPLAY_ORIGIN
                        && lookupElement.completionMetadata != null
                        && lookupElement.completionMetadata.getOrigin() != null) {
                      typeText += lookupElement.completionMetadata.getOrigin().toString();
                    } else {
                      typeText += StaticConfig.BRAND_NAME;
                    }

                    presentation.setTypeText(typeText);
                    presentation.setItemTextBold(false);
                    presentation.setStrikeout(
                        lookupElement.completionMetadata != null
                            && lookupElement.completionMetadata.getIsDeprecated());
                    presentation.setItemText(lookupElement.newPrefix);
                    presentation.setIcon(ICON);
                  }
                });
    if (locked) {
      final LimitExceededLookupElement lookupElement =
          new LimitExceededLookupElement(lookupElementBuilder);
      if (activeLookup != null) {
        activeLookup.addLookupListener(lookupElement);
      }
      return lookupElement;
    } else {
      lookupElementBuilder =
          lookupElementBuilder.withInsertHandler(
              (context, item) -> {
                int end = context.getTailOffset();
                TabNineCompletion lookupElement = (TabNineCompletion) item.getObject();
                try {
                  context
                      .getDocument()
                      .insertString(
                          end + lookupElement.oldSuffix.length(), lookupElement.newSuffix);
                  context.getDocument().deleteString(end, end + lookupElement.oldSuffix.length());
                  if (AppSettingsState.getInstance().getAutoImportEnabled()) {
                    Logger.getInstance(getClass()).info("Registering auto importer");
                    AutoImporter.registerTabNineAutoImporter(
                        context.getEditor(),
                        context.getProject(),
                        context.getStartOffset(),
                        context.getTailOffset());
                  }
                } catch (RuntimeException re) {
                  Logger.getInstance(getClass())
                      .warn(
                          "Error inserting new suffix. End = "
                              + end
                              + ", old suffix length = "
                              + lookupElement.oldSuffix.length()
                              + ", new suffix length = "
                              + lookupElement.newSuffix.length(),
                          re);
                }
              });
    }
    return lookupElementBuilder;
  }

  private void addAdvertisement(
      @NotNull CompletionResultSet result, AutocompleteResponse completions) {
    if (completions.user_message.length >= 1) {
      String details = String.join(" ", completions.user_message);

      details = details.substring(0, Math.min(details.length(), ADVERTISEMENT_MAX_LENGTH));

      result.addLookupAdvertisement(details);
    }
  }

  private void registerLookupListener(
      CompletionParameters parameters, LookupListener lookupListener) {
    final LookupEx lookupEx = LookupManager.getActiveLookup(parameters.getEditor());
    if (lookupEx == null) {
      return;
    }
    lookupEx.removeLookupListener(lookupListener);
    lookupEx.addLookupListener(lookupListener);
  }
}
