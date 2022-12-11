package com.tabnine.general;

import com.tabnine.UninstallListener;
import com.tabnine.binary.*;
import com.tabnine.binary.fetch.*;
import com.tabnine.capabilities.SuggestionsModeService;
import com.tabnine.hover.HoverUpdater;
import com.tabnine.inline.CompletionPreviewToggleEventSender;
import com.tabnine.inline.InlineCompletionHandler;
import com.tabnine.inline.TabnineInlineLookupListener;
import com.tabnine.lifecycle.BinaryInstantiatedActions;
import com.tabnine.lifecycle.BinaryNotificationsLifecycle;
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnine.lifecycle.UninstallReporter;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.selections.CompletionPreviewListener;
import com.tabnine.selections.TabNineLookupListener;
import com.tabnine.statusBar.StatusBarUpdater;
import org.jetbrains.annotations.NotNull;

public class DependencyContainer {
  public static int binaryRequestsConsecutiveTimeoutsThreshold =
      StaticConfig.CONSECUTIVE_TIMEOUTS_THRESHOLD;
  public static int binaryRequestConsecutiveRestartsThreshold =
      StaticConfig.CONSECUTIVE_RESTART_THRESHOLD;
  private static BinaryProcessRequesterProvider BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE = null;
  private static InlineCompletionHandler INLINE_COMPLETION_HANDLER_INSTANCE = null;

  // For Integration Tests
  private static BinaryRun binaryRunMock = null;
  private static BinaryProcessGatewayProvider binaryProcessGatewayProviderMock = null;
  private static BinaryProcessRequesterPoller poller = null;

  private static SuggestionsModeService suggestionsModeServiceMock = null;

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

  public static CompletionPreviewListener instanceOfCompletionPreviewListener() {
    final BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
    return new CompletionPreviewListener(
        binaryRequestFacade, new StatusBarUpdater(binaryRequestFacade), new HoverUpdater());
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
    return new UninstallListener(instanceOfBinaryRequestFacade(), instanceOfUninstallReporter());
  }

  public static CompletionPreviewToggleEventSender instanceOfCompletionPreviewToggleEventSender() {
    return new CompletionPreviewToggleEventSender(instanceOfBinaryRequestFacade());
  }

  public static void setTesting(
      BinaryRun binaryRunMock,
      BinaryProcessGatewayProvider binaryProcessGatewayProviderMock,
      BinaryProcessRequesterPoller poller,
      SuggestionsModeService suggestionsModeServiceMock,
      int binaryRequestsTimeoutsThreshold,
      int binaryRequestRestartsThreshold) {
    DependencyContainer.binaryRunMock = binaryRunMock;
    DependencyContainer.binaryProcessGatewayProviderMock = binaryProcessGatewayProviderMock;
    DependencyContainer.poller = poller;
    DependencyContainer.suggestionsModeServiceMock = suggestionsModeServiceMock;
    DependencyContainer.binaryRequestsConsecutiveTimeoutsThreshold =
        binaryRequestsTimeoutsThreshold;
    DependencyContainer.binaryRequestConsecutiveRestartsThreshold = binaryRequestRestartsThreshold;
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
              instanceOfRequestPoller(),
              binaryRequestsConsecutiveTimeoutsThreshold,
              binaryRequestConsecutiveRestartsThreshold);
    }

    return BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE;
  }

  private static BinaryProcessGatewayProvider instanceOfBinaryProcessGatewayProvider() {
    if (binaryProcessGatewayProviderMock != null) {
      return binaryProcessGatewayProviderMock;
    }

    return new BinaryProcessGatewayProvider();
  }

  private static BinaryProcessRequesterPoller instanceOfRequestPoller() {
    if (poller != null) {
      return poller;
    }
    return new BinaryProcessRequesterPollerCappedImpl(10, 100, 1000);
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
        instanceOfBundleDownloader());
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
