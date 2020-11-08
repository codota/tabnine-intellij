package com.tabnine.general;

import com.intellij.ide.plugins.PluginStateListener;
import com.tabnine.binary.BinaryFacade;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.BinaryRun;
import com.tabnine.binary.TabNineGateway;
import com.tabnine.binary.fetch.*;
import com.tabnine.lifecycle.TabNineDisablePluginListener;
import com.tabnine.lifecycle.TabNinePluginStateListener;
import com.tabnine.lifecycle.UninstallReporter;
import com.tabnine.prediction.CompletionFacade;
import com.tabnine.selections.TabNineLookupListener;
import org.jetbrains.annotations.NotNull;

public class DependencyContainer {
    private static TabNineGateway TABNINE_GATEWAY_INSTANCE = null;
    private static BinaryRequestFacade BINARY_REQUEST_FACADE_INSTANCE = null;
    private static TabNineLookupListener LOOKUP_LISTENER_INSTANCE = null;
    private static TabNineDisablePluginListener DISABLE_PLUGIN_LISTENER_INSTANCE = null;

    public static TabNineDisablePluginListener singletonOfTabNineDisablePluginListener() {
        if (DISABLE_PLUGIN_LISTENER_INSTANCE == null) {
            DISABLE_PLUGIN_LISTENER_INSTANCE = new TabNineDisablePluginListener(instanceOfUninstallReporter(), singletonOfBinaryRequestFacade());
        }

        return DISABLE_PLUGIN_LISTENER_INSTANCE;
    }

    public static BinaryRequestFacade singletonOfBinaryRequestFacade() {
        if (BINARY_REQUEST_FACADE_INSTANCE == null) {
            BINARY_REQUEST_FACADE_INSTANCE = new BinaryRequestFacade(singletonOfTabNineGateway());
        }

        return BINARY_REQUEST_FACADE_INSTANCE;
    }

    public static synchronized TabNineLookupListener singletonOfTabNineLookupListener() {
        if (LOOKUP_LISTENER_INSTANCE == null) {
            LOOKUP_LISTENER_INSTANCE = new TabNineLookupListener(singletonOfBinaryRequestFacade());
        }

        return LOOKUP_LISTENER_INSTANCE;
    }

    private static synchronized TabNineGateway singletonOfTabNineGateway() {
        if (TABNINE_GATEWAY_INSTANCE == null) {
            TABNINE_GATEWAY_INSTANCE = new TabNineGateway();
            TABNINE_GATEWAY_INSTANCE.init();
        }

        return TABNINE_GATEWAY_INSTANCE;
    }

    @NotNull
    public static CompletionFacade instanceOfCompletionFacade() {
        return new CompletionFacade(singletonOfBinaryRequestFacade());
    }

    @NotNull
    public static BinaryFacade instanceOfBinaryFacade() {
        return new BinaryFacade(instanceOfBinaryRun());
    }

    @NotNull
    public static PluginStateListener instanceOfTabNinePluginStateListener() {
        return new TabNinePluginStateListener(instanceOfUninstallReporter(), singletonOfBinaryRequestFacade());
    }

    private static UninstallReporter instanceOfUninstallReporter() {
        return new UninstallReporter(instanceOfBinaryRun());
    }

        @NotNull
    private static BinaryRun instanceOfBinaryRun() {
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
