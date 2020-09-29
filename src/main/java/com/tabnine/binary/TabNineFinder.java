package com.tabnine.binary;

import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tabnine.StaticConfig.*;
import static java.util.Collections.emptyList;

public class TabNineFinder {
    /**
     * Fetchs TabNine's preferred version from remote server, downloads it if it is not available, and returns the path to run it.
     *
     * @return path to run
     * @throws IOException if something went wrong
     */
    public static String fetchTabNineBinary() {
        List<String> versions = listExistingVersions();
        try {
            String preferredVersion = fetchPreferredVersion();

            downloadTabNineVersionIfNotExists(versions, preferredVersion);

            return versionPath(preferredVersion);
        } catch (FailedToDownloadException e) {
            // TODO: log warning about the exception.
            String foundPath = useLatestAvailableVersion(versions);

            if (foundPath != null) {
                return foundPath;
            }
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

    private static void downloadTabNineVersionIfNotExists(List<String> versions, String version) throws FailedToDownloadException {
        if (versions.contains(version)) {
            return;
        }
        byte[] exe = downloadBinary(version);
        String dst = Paths.get(BINARY_DIRECTORY.toString(), version, TARGET_NAME, EXECUTABLE_NAME).toString();
        Path tmpDst = Paths.get(dst + ".download." + UUID.randomUUID().toString());
        writeBytes(exe, tmpDst);

        try {
            tmpDst.toFile().renameTo(dst.toFile());
        } catch (Throwable t) {
        }

        if (!dst.toFile().exists()) throw new IOException("TabNine binary not found");

        return dst.toString();
    }

    @NotNull
    private static byte[] downloadBinary(String version) throws FailedToDownloadException {
        String url = String.join("/", CDN_URL, version, TARGET_NAME, EXECUTABLE_NAME);
        byte[] exe = download(url);

        if (exe.length < 1000 * 1000) {
            throw new FailedToDownloadException("Couldn't get TabNine from " + url);
        }

        return exe;
    }

    static void writeBytes(byte[] b, Path dst) throws IOException {
        dst.getParent().toFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(dst.toString())) {
            fos.write(b);
        }
        System.err.println("Wrote " + b.length + " bytes to " + dst.toString());
        if (!tmpDst.toFile().setExecutable(true)) {
            throw new IOException("Could not set execute permission on " + dst);
        }
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

}
