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

    public BinaryDownloader(TempBinaryValidator tempBinaryValidator) {
        this.tempBinaryValidator = tempBinaryValidator;
    }

    public Optional<BinaryVersion> downloadBinary(String version) {
        String destination = versionFullPath(version);
        Path tempDestination = Paths.get(format("%s.download.%s", destination, UUID.randomUUID()));

        try {
            if (!tempDestination.getParent().toFile().mkdirs()) {
                Logger.getInstance(getClass()).warn(format("Could not create the required directories for %s", tempDestination));

                return Optional.empty();
            }

            URLConnection connection = new URL(String.join("/", getServerUrl(), version, TARGET_NAME, EXECUTABLE_NAME)).openConnection();

            connection.setConnectTimeout(REMOTE_CONNECTION_TIMEOUT);
            connection.setReadTimeout(BINARY_READ_TIMEOUT);

            Files.copy(connection.getInputStream(), tempDestination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Logger.getInstance(getClass()).warn(e);

            return Optional.empty();
        }

        try {
            tempBinaryValidator.validateAndRename(tempDestination, Paths.get(destination));
        } catch (FailedToDownloadException e) {
            Logger.getInstance(getClass()).warn(e);

            return Optional.empty();
        }

        Logger.getInstance(getClass()).info(format("New binary version %s downloaded successfully.", version));

        return Optional.of(new BinaryVersion(destination, version));
    }

}
