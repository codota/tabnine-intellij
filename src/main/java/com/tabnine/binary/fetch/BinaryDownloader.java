package com.tabnine.binary.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.binary.exceptions.FailedToDownloadException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import static com.tabnine.general.StaticConfig.*;
import static java.lang.String.format;

public class BinaryDownloader {
    private final TempBinaryValidator tempBinaryValidator;
    private final GeneralDownloader downloader;
    public BinaryDownloader(TempBinaryValidator tempBinaryValidator, GeneralDownloader downloader) {
        this.tempBinaryValidator = tempBinaryValidator;
        this.downloader = downloader;
    }

    public Optional<BinaryVersion> downloadBinary(String version) {
        String urlString = String.join("/", getServerUrl(), version, TARGET_NAME, EXECUTABLE_NAME);
        String destination = versionFullPath(version);
        if (this.downloader.download(urlString, destination, tempBinaryValidator)) {
             return Optional.of(new BinaryVersion(destination, version));
        }
        return Optional.empty();
    }

}
