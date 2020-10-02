package com.tabnine.binary.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.StaticConfig;
import com.tabnine.binary.FailedToDownloadException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static com.tabnine.StaticConfig.*;
import static java.lang.String.format;

public class BinaryDownloader {
    private final TempBinaryValidator tempBinaryValidator;

    public BinaryDownloader(TempBinaryValidator tempBinaryValidator) {
        this.tempBinaryValidator = tempBinaryValidator;
    }

    public void downloadBinary(String version) throws FailedToDownloadException {
        Path destination = versionFullPath(version);
        Path tempDestination = Paths.get(format("%s.download.%s", destination, UUID.randomUUID()));

        try {
            if (!tempDestination.getParent().toFile().mkdirs()) {
                throw new FailedToDownloadException("Could not create the required directories for " + tempDestination.toString());
            }

            URLConnection connection = new URL(String.join("/", getServerUrl(), version, TARGET_NAME, EXECUTABLE_NAME)).openConnection();

            connection.setConnectTimeout(REMOTE_CONNECTION_TIMEOUT);
            connection.setReadTimeout(BINARY_READ_TIMEOUT);

            Files.copy(connection.getInputStream(), tempDestination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FailedToDownloadException(e);
        }

        tempBinaryValidator.validateAndRename(tempDestination, destination);
        Logger.getInstance(getClass()).info(format("New binary version %s downloaded successfully.", version));
    }

}
