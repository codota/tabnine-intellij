package com.tabnine.general;

import com.intellij.ide.plugins.PluginStateListener;
import com.tabnine.binary.*;
import com.tabnine.binary.fetch.*;
import com.tabnine.lifecycle.TabNineDisablePluginListener;
import com.tabnine.lifecycle.TabNinePluginStateListener;
import com.tabnine.lifecycle.UninstallReporter;
import com.tabnine.notifications.BinaryNotifications;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.selections.TabNineLookupListener;
import org.jetbrains.annotations.NotNull;

public class DependencyContainer {
    private static TabNineDisablePluginListener DISABLE_PLUGIN_LISTENER_INSTANCE = null;
    private static BinaryProcessRequesterProvider BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE = null;

    // For Integration Tests
    private static BinaryRun binaryRunMock = null;
    private static BinaryProcessGatewayProvider binaryProcessGatewayProviderMock = null;

    public static TabNineDisablePluginListener singletonOfTabNineDisablePluginListener() {
        if (DISABLE_PLUGIN_LISTENER_INSTANCE == null) {
            DISABLE_PLUGIN_LISTENER_INSTANCE = new TabNineDisablePluginListener(instanceOfUninstallReporter(), instanceOfBinaryRequestFacade());
        }

        return DISABLE_PLUGIN_LISTENER_INSTANCE;
    }

    public static synchronized TabNineLookupListener instanceOfTabNineLookupListener() {
        return new TabNineLookupListener(instanceOfBinaryRequestFacade());
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
        return new TabNinePluginStateListener(instanceOfUninstallReporter(), instanceOfBinaryRequestFacade());
    }

    public static BinaryNotifications instanceOfBinaryNotifications() {
        return new BinaryNotifications(instanceOfBinaryRequestFacade());
    }

    public static void setTesting(BinaryRun binaryRunMock, BinaryProcessGatewayProvider binaryProcessGatewayProviderMock) {
        DependencyContainer.binaryRunMock = binaryRunMock;
        DependencyContainer.binaryProcessGatewayProviderMock = binaryProcessGatewayProviderMock;
    }

    private static BinaryProcessRequesterProvider singletonOfBinaryProcessRequesterProvider() {
        if (BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE == null) {
            BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE = BinaryProcessRequesterProvider.create(instanceOfBinaryRun(), instanceOfBinaryProcessGatewayProvider());
        }

        return BINARY_PROCESS_REQUESTER_PROVIDER_INSTANCE;
    }

    private static BinaryProcessGatewayProvider instanceOfBinaryProcessGatewayProvider() {
        if(binaryProcessGatewayProviderMock != null) {
            return binaryProcessGatewayProviderMock;
        }

        return new BinaryProcessGatewayProvider();
    }

    private static UninstallReporter instanceOfUninstallReporter() {
        return new UninstallReporter(instanceOfBinaryRun());
    }

    @NotNull
    private static BinaryRun instanceOfBinaryRun() {
        if(binaryRunMock != null) {
            return binaryRunMock;
        }

        return new BinaryRun(instanceOfBinaryFetcher());
    }

    @NotNull
    private static BinaryVersionFetcher instanceOfBinaryFetcher() {
        return new BinaryVersionFetcher(instanceOfLocalBinaryVersions(), instanceOfBinaryRemoteSource(), instanceOfBinaryDownloader(), instanceOfBundleDownloader());
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
