package com.tabnine;

import com.tabnine.binary.BinaryFacade;
import com.tabnine.binary.BinaryRun;
import com.tabnine.binary.fetch.*;
import org.jetbrains.annotations.NotNull;

public class DependencyContainer {
    @NotNull
    public static BinaryFacade instanceOfBinaryFacade() {
        return new BinaryFacade(instanceOfBinaryRun());
    }

    @NotNull
    private static BinaryRun instanceOfBinaryRun() {
        return new BinaryRun(instanceOfBinaryFetcher());
    }

    @NotNull
    private static BinaryVersionFetcher instanceOfBinaryFetcher() {
        return new BinaryVersionFetcher(instanceOfLocalBinaryVersions(), instanceOfBinaryRemoteSource(), instanceOfBinaryDownloader());
    }

    @NotNull
    private static BinaryDownloader instanceOfBinaryDownloader() {
        return new BinaryDownloader(instanceOfBinaryPropositionValidator());
    }

    @NotNull
    private static TempBinaryValidator instanceOfBinaryPropositionValidator() {
        return new TempBinaryValidator(instanceOfBinaryValidator());
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
