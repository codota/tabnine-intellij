package com.tabnine.general;

import com.tabnineCommon.UninstallListener;
import com.tabnineCommon.binary.*;
import com.tabnineCommon.binary.fetch.*;
import com.tabnineCommon.capabilities.SuggestionsModeService;
import com.tabnineCommon.general.*;
import com.tabnineCommon.hover.HoverUpdater;
import com.tabnineCommon.inline.InlineCompletionHandler;
import com.tabnineCommon.inline.TabnineInlineLookupListener;
import com.tabnineCommon.lifecycle.BinaryInstantiatedActions;
import com.tabnineCommon.lifecycle.BinaryNotificationsLifecycle;
import com.tabnineCommon.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnineCommon.lifecycle.UninstallReporter;
import com.tabnineCommon.prediction.CompletionFacade;
import com.tabnineCommon.selections.CompletionPreviewListener;
import com.tabnineCommon.selections.TabNineLookupListener;
import com.tabnineCommon.statusBar.StatusBarUpdater;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DependencyContainer implements IProviderOfThings {
  public static int binaryRequestsTimeoutsThresholdMillis =
      StaticConfig.BINARY_TIMEOUTS_THRESHOLD_MILLIS;
  private static BinaryProcessRequesterProvider BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE = null;
  private static InlineCompletionHandler INLINE_COMPLETION_HANDLER_INSTANCE = null;

  // For Integration Tests
  private static BinaryRun binaryRunMock = null;
  private static BinaryProcessGatewayProvider binaryProcessGatewayProviderMock = null;
  private static SuggestionsModeService suggestionsModeServiceMock = null;
  private static CompletionsEventSender completionsEventSender = null;

  private static final String serverUrl = StaticConfig.getTabNineBundleVersionUrl().orElse(null);

  @NotNull
  @Override
  public BinaryRequestFacade getBinaryRequestFacade() {
    return instanceOfBinaryRequestFacade();
  }

  @NotNull
  @Override
  public SuggestionsModeService getSuggestionsModeService() {
    return instanceOfSuggestionsModeService();
  }

  @NotNull
  @Override
  public ISubscriptionType getSubscriptionType(@Nullable ServiceLevel serviceLevel) {
    return SubscriptionType.getSubscriptionType(serviceLevel);
  }

  @NotNull
  @Override
  public CompletionsEventSender getCompletionsEventSender() {
    return instanceOfCompletionsEventSender();
  }

  @NotNull
  @Override
  public InlineCompletionHandler getInlineCompletionHandler() {
    return singletonOfInlineCompletionHandler();
  }

  @NotNull
  @Override
  public CompletionPreviewListener getCompletionPreviewListener() {
    final BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
    return new CompletionPreviewListener(
        binaryRequestFacade, new StatusBarUpdater(binaryRequestFacade), new HoverUpdater());
  }

  public static synchronized TabNineLookupListener instanceOfTabNineLookupListener() {
    final BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
    return new TabNineLookupListener(
        binaryRequestFacade,
        new StatusBarUpdater(binaryRequestFacade),
        instanceOfSuggestionsModeService());
  }

  public static synchronized TabnineInlineLookupListener instanceOfTabNineInlineLookupListener() {
    return new TabnineInlineLookupListener();
  }

  public static BinaryRequestFacade instanceOfBinaryRequestFacade() {
    return new BinaryRequestFacade(singletonOfBinaryProcessRequesterProvider());
  }

  public static InlineCompletionHandler singletonOfInlineCompletionHandler() {
    if (INLINE_COMPLETION_HANDLER_INSTANCE == null) {
      INLINE_COMPLETION_HANDLER_INSTANCE =
          new InlineCompletionHandler(
              instanceOfCompletionFacade(),
              instanceOfBinaryRequestFacade(),
              instanceOfSuggestionsModeService());
    }

    return INLINE_COMPLETION_HANDLER_INSTANCE;
  }

  @NotNull
  public static CompletionFacade instanceOfCompletionFacade() {
    return new CompletionFacade(
        instanceOfBinaryRequestFacade(), instanceOfSuggestionsModeService());
  }

  public static BinaryNotificationsLifecycle instanceOfBinaryNotifications() {
    return new BinaryNotificationsLifecycle(
        instanceOfBinaryRequestFacade(), instanceOfGlobalActionVisitor());
  }

  public static BinaryInstantiatedActions instanceOfGlobalActionVisitor() {
    return new BinaryInstantiatedActions(instanceOfBinaryRequestFacade());
  }

  public static BinaryPromotionStatusBarLifecycle instanceOfBinaryPromotionStatusBar() {
    return new BinaryPromotionStatusBarLifecycle(
        new StatusBarUpdater(instanceOfBinaryRequestFacade()));
  }

  public static UninstallListener instanceOfUninstallListener() {
    return new UninstallListener(
        instanceOfBinaryRequestFacade(), instanceOfUninstallReporter(), null);
  }

  public static void setTesting(
      BinaryRun binaryRunMock,
      BinaryProcessGatewayProvider binaryProcessGatewayProviderMock,
      SuggestionsModeService suggestionsModeServiceMock,
      CompletionsEventSender completionsEventSenderMock,
      int binaryRequestsTimeoutsThreshold) {
    DependencyContainer.binaryRunMock = binaryRunMock;
    DependencyContainer.binaryProcessGatewayProviderMock = binaryProcessGatewayProviderMock;
    DependencyContainer.suggestionsModeServiceMock = suggestionsModeServiceMock;
    DependencyContainer.completionsEventSender = completionsEventSenderMock;
    DependencyContainer.binaryRequestsTimeoutsThresholdMillis = binaryRequestsTimeoutsThreshold;
  }

  public static SuggestionsModeService instanceOfSuggestionsModeService() {
    if (suggestionsModeServiceMock != null) {
      return suggestionsModeServiceMock;
    }

    return new SuggestionsModeService();
  }

  private static BinaryProcessRequesterProvider singletonOfBinaryProcessRequesterProvider() {
    if (BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE == null) {
      BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE =
          BinaryProcessRequesterProvider.create(
              instanceOfBinaryRun(),
              instanceOfBinaryProcessGatewayProvider(),
              // This is the case where we SHOULD NOT have different url, as it is the public
              // plugin
              null,
              binaryRequestsTimeoutsThresholdMillis);
    }

    return BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE;
  }

  private static BinaryProcessGatewayProvider instanceOfBinaryProcessGatewayProvider() {
    return Objects.requireNonNullElseGet(
        binaryProcessGatewayProviderMock, BinaryProcessGatewayProvider::new);
  }

  public static CompletionsEventSender instanceOfCompletionsEventSender() {
    if (completionsEventSender != null) {
      return completionsEventSender;
    }

    return new CompletionsEventSender(instanceOfBinaryRequestFacade());
  }

  private static UninstallReporter instanceOfUninstallReporter() {
    return new UninstallReporter(instanceOfBinaryRun());
  }

  @NotNull
  private static BinaryRun instanceOfBinaryRun() {
    if (binaryRunMock != null) {
      return binaryRunMock;
    }

    return new BinaryRun(instanceOfBinaryFetcher());
  }

  @NotNull
  private static BinaryVersionFetcher instanceOfBinaryFetcher() {
    return new BinaryVersionFetcher(
        instanceOfLocalBinaryVersions(),
        instanceOfBinaryRemoteSource(),
        instanceOfBinaryDownloader(),
        instanceOfBundleDownloader(),
        serverUrl);
  }

  @NotNull
  private static BinaryDownloader instanceOfBinaryDownloader() {
    return new BinaryDownloader(instanceOfBinaryPropositionValidator(), instanceOfDownloader());
  }

  @NotNull
  private static BundleDownloader instanceOfBundleDownloader() {
    return new BundleDownloader(instanceOfBundlePropositionValidator(), instanceOfDownloader());
  }

  @NotNull
  private static GeneralDownloader instanceOfDownloader() {
    return new GeneralDownloader();
  }

  @NotNull
  private static TempBinaryValidator instanceOfBinaryPropositionValidator() {
    return new TempBinaryValidator(instanceOfBinaryValidator());
  }

  @NotNull
  private static TempBundleValidator instanceOfBundlePropositionValidator() {
    return new TempBundleValidator();
  }

  @NotNull
  private static BinaryRemoteSource instanceOfBinaryRemoteSource() {
    return new BinaryRemoteSource();
  }

  @NotNull
  private static LocalBinaryVersions instanceOfLocalBinaryVersions() {
    return new LocalBinaryVersions(instanceOfBinaryValidator());
  }

  @NotNull
  private static BinaryValidator instanceOfBinaryValidator() {
    return new BinaryValidator();
  }
}
