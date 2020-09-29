package com.tabnine.binary;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.util.PlatformUtils;
import com.intellij.util.text.SemVer;
import com.tabnine.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tabnine.StaticConfig.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class TabNineFinder {
    /**
     * Fetchs TabNine's preferred version from remote server, downloads it if it is not available, and returns the path to run it.
     *
     * @return path to run
     * @throws IOException if something went wrong
     */
    public static String fetchTabNineBinary() throws SecurityException, NoExistingBinaryException {
        List<String> versions = listExistingVersions();
        try {
            String preferredVersion = fetchPreferredVersion();

            return downloadTabNineVersionIfNotExists(versions, preferredVersion);
        } catch (FailedToDownloadException e) {
            // TODO: log warning about the exception.
            return useLatestAvailableVersion(versions);
        }
    }

    private static String useLatestAvailableVersion(List<String> versions) throws NoExistingBinaryException {
        return versions.stream().map(SemVer::parseFromText).filter(Objects::nonNull).max(SemVer::compareTo)
                .map(SemVer::toString).orElseThrow(NoExistingBinaryException::new);
    }

    private static String versionPath(String preferredVersion) {
        String dir = BINARY_DIRECTORY.toString();
        for (String child : children) {
            Path candidate = Paths.get(dir, child, TARGET_NAME, EXECUTABLE_NAME);
            if (candidate.toFile().exists()) {
                return candidate.toString();
            }
        }
        return null;
    }

    private static boolean hasExistingVersion(String preferredVersion) {
        return isVersionWorking(preferredVersion);
    }

    @NotNull
    private static String fetchPreferredVersion() throws FailedToDownloadException {
        return new String(download(CDN_URL + "/version")).trim();
    }

    @NotNull
    private static List<String> listExistingVersions() {
        File[] versionsFolders = BINARY_DIRECTORY.toFile().listFiles();

        if (versionsFolders != null) {
            return Stream.of(versionsFolders).map(File::getName).collect(Collectors.toList());
        }

        return emptyList();
    }

    private static boolean isVersionWorking(String path) {
        ProcessBuilder binary = new ProcessBuilder(path, "--print-version");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(binary.start().getInputStream(), StandardCharsets.UTF_8))) {
            if (reader.readLine() == null) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private static String downloadTabNineVersionIfNotExists(List<String> versions, String version) throws FailedToDownloadException {
        Path destination = getVersionFullPath(version);

        if (!versions.contains(version)) {
            Path tmpDestination = Paths.get(format("%s.download.%s", destination, UUID.randomUUID()));

            downloadBinary(version, tmpDestination);

            moveBinaryFromTemp(destination, tmpDestination);
        }

        return destination.toString();
    }

    private static void moveBinaryFromTemp(Path destination, Path tmpDestination) throws FailedToDownloadException {
        if (!tmpDestination.toFile().renameTo(destination.toFile()) || !destination.toFile().exists()) {
            throw new FailedToDownloadException("Although downloaded successfully and without errors, TabNine's binary does not exists in the detination folder: " + destination.toString());
        }
    }

    private static void downloadBinary(String version, Path destination) throws FailedToDownloadException {
        String binaryDownloadURL = String.join("/", CDN_URL, version, TARGET_NAME, EXECUTABLE_NAME);

        try {
            if(!destination.getParent().toFile().mkdirs()) {
                throw new FailedToDownloadException("Could not create the required directories for " + destination.toString());
            }

            Files.copy(new URL(binaryDownloadURL).openConnection().getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // TODO: Log warning that I failed to download from url.
            throw new FailedToDownloadException(e);
        }

        long fileSize = destination.toFile().length();
        if (fileSize < 1000 * 1000) {
            throw new FailedToDownloadException("Couldn't download TabNine's binary from " + binaryDownloadURL);
        }

        if (!destination.toFile().setExecutable(true)) {
            throw new FailedToDownloadException("Couldn't set execute permission on " + destination);
        }

        // TODO: Change this to human readable message that is written to the log.
        System.err.println("Wrote " + fileSize + " bytes to " + destination.toString());
    }

    @NotNull
    private static Path getVersionFullPath(String version) {
        return Paths.get(BINARY_DIRECTORY.toString(), version, TARGET_NAME, EXECUTABLE_NAME);
    }

    static byte[] download(String urlString) throws FailedToDownloadException {
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            return toBytes(is);
        } catch (IOException e) {
            // TODO: Log warning that I failed to download from url.
            throw new FailedToDownloadException(e);
        }
    }

    static byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    // FIXME: This code is the highest risk code that is not tested at all.
    @NotNull
    public static List<String> generateCommand() throws NoExistingBinaryException {
        // When we tell TabNine that it's talking to IntelliJ, it won't suggest language server
        // setup since we assume it's already built into the IDE
        List<String> command = new ArrayList<>(singletonList(fetchTabNineBinary()));
        List<String> metadata = new ArrayList<>();
        metadata.add("--client-metadata");
        metadata.add("pluginVersion=" + Utils.getPluginVersion());
        metadata.add("clientIsUltimate=" + PlatformUtils.isIdeaUltimate());
        final ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        if (applicationInfo != null) {
            command.add("--client");
            command.add(applicationInfo.getVersionName());
            command.add("--no-lsp");
            command.add("true");
            metadata.add("clientVersion=" + applicationInfo.getFullVersion());
            metadata.add("clientApiVersion=" + applicationInfo.getApiVersion());
        }
        command.addAll(metadata);

        return command;
    }
}
