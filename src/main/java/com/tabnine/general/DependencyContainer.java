package com.tabnine.general;

import com.intellij.ide.plugins.PluginStateListener;
import com.tabnine.binary.*;
import com.tabnine.binary.fetch.*;
import com.tabnine.lifecycle.*;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.selections.CompletionPreviewListener;
import com.tabnine.selections.TabNineLookupListener;
import com.tabnine.statusBar.StatusBarUpdater;
import org.jetbrains.annotations.NotNull;

public class DependencyContainer {
  private static BinaryProcessRequesterProvider BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE = null;

  // For Integration Tests
  private static BinaryRun binaryRunMock = null;
  private static BinaryProcessGatewayProvider binaryProcessGatewayProviderMock = null;
  private static BinaryProcessRequesterPoller poller = null;

  public static synchronized TabNineLookupListener instanceOfTabNineLookupListener() {
    final BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
    return new TabNineLookupListener(
        binaryRequestFacade, new StatusBarUpdater(binaryRequestFacade));
  }

  public static CompletionPreviewListener instanceOfCompletionPreviewListener() {
    final BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
    return new CompletionPreviewListener(
        binaryRequestFacade, new StatusBarUpdater(binaryRequestFacade));
  }

  public static BinaryRequestFacade instanceOfBinaryRequestFacade() {
    return new BinaryRequestFacade(singletonOfBinaryProcessRequesterProvider());
  }

  @NotNull
  public static CompletionFacade instanceOfCompletionFacade() {
    return new CompletionFacade(instanceOfBinaryRequestFacade());
  }

  @NotNull
  public static PluginStateListener instanceOfTabNinePluginStateListener() {
    return new TabNinePluginStateListener(
        instanceOfUninstallReporter(), instanceOfBinaryRequestFacade());
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

  public static void setTesting(
      BinaryRun binaryRunMock,
      BinaryProcessGatewayProvider binaryProcessGatewayProviderMock,
      BinaryProcessRequesterPoller poller) {
    DependencyContainer.binaryRunMock = binaryRunMock;
    DependencyContainer.binaryProcessGatewayProviderMock = binaryProcessGatewayProviderMock;
    DependencyContainer.poller = poller;
  }

  private static BinaryProcessRequesterProvider singletonOfBinaryProcessRequesterProvider() {
    if (BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE == null) {
      BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE =
          BinaryProcessRequesterProvider.create(
              instanceOfBinaryRun(),
              instanceOfBinaryProcessGatewayProvider(),
              instanceOfRequestPoller());
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
