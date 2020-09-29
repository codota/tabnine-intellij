package com.tabnine.binary;

import com.intellij.openapi.util.SystemInfo;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import static com.tabnine.StaticConfig.*;

public class TabNineFinder {
    public static String getTabNinePath() throws IOException {
        String[] children;
        try {
            File dir = getTabNineDirectory().toFile();
            File[] fileChildren = dir.listFiles();
            if (fileChildren == null) {
                children = new String[0];
            } else {
                children = Stream.of(fileChildren).map(File::getName).toArray(String[]::new);
            }
        } catch (IOException e) {
            children = new String[0];
        }
        String foundPath = searchForTabNine(children);
        String version = new String(download(CDN_URL + "/version")).trim();
        if (foundPath != null && shouldUseCurrentTabNineInstallation(foundPath)) {
            return foundPath;
        }
        return downloadTabNine(version);
    }

    private static String getInstallationVersion(String path) throws IOException {
        String version;
        ProcessBuilder builder = new ProcessBuilder(path, "--print-version");
        final Process process = builder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            version = reader.readLine();
        }
        if (version == null) {
            throw new IOException("Could not get TabNine binary version");
        }
        return version;
    }

    private static boolean shouldUseCurrentTabNineInstallation(String tabNinePath) {
        try {
            String currentVersion = getInstallationVersion(tabNinePath);
            return parseSemver(currentVersion).compareTo(parseSemver("2.8.1")) > 0;
        } catch (IOException e) {
            return false;
        }
    }

    static String downloadTabNine(String version) throws IOException {
        String url = String.join("/", new String[]{CDN_URL, version, getTargetName(), getExeName()});
        byte[] exe = download(url);
        if (exe.length < 1000 * 1000) {
            throw new IOException("Couldn't get TabNine from " + url);
        }
        Path dst = Paths.get(getTabNineDirectory().toString(), version, getTargetName(), getExeName());
        Path tmpDst = Paths.get(dst.toString() + ".download." + UUID.randomUUID().toString());
        writeBytes(exe, tmpDst);
        if (!tmpDst.toFile().setExecutable(true)) {
            throw new IOException("Could not set execute permission on " + dst.toString());
        }

        try {
            tmpDst.toFile().renameTo(dst.toFile());
        } catch (Throwable t) {
        }

        if (!dst.toFile().exists()) throw new IOException("TabNine binary not found");

        return dst.toString();
    }

    static void writeBytes(byte[] b, Path dst) throws IOException {
        dst.getParent().toFile().mkdirs();
        try(FileOutputStream fos = new FileOutputStream(dst.toString())) {
            fos.write(b);
        }
        System.err.println("Wrote " + b.length + " bytes to " + dst.toString());
    }

    static byte[] download(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        return toBytes(is);
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

    static String getExeName() {
        return SystemInfo.isWindows ? "TabNine.exe" : "TabNine";
    }

    static String searchForTabNine(String[] children) throws IOException{
        Arrays.sort(children, (a, b) -> parseSemver(b).compareTo(parseSemver(a)));

        String dir = getTabNineDirectory().toString();
        String target = getTargetName();
        String exe = getExeName();
        for (String child : children) {
            Path candidate = Paths.get(dir, child, target, exe);
            if (candidate.toFile().exists()) {
                return candidate.toString();
            }
        }
        return null;
    }

    static String getTargetName() {
        String is32or64;
        if (SystemInfo.is32Bit) {
            is32or64 = "i686";
        } else {
            is32or64 = "x86_64";
        }
        String platform;
        if (SystemInfo.isWindows) {
            platform = "pc-windows-gnu";
        } else if (SystemInfo.isMac) {
            platform = "apple-darwin";
        } else if (SystemInfo.isLinux) {
            platform = "unknown-linux-musl";
        } else if (SystemInfo.isFreeBSD) {
            platform = "unknown-freebsd";
        } else {
            throw new RuntimeException("Platform was not recognized as any of Windows, macOS, Linux, FreeBSD");
        }
        return is32or64 + "-" + platform;
    }

    static String parseSemver(String version) {
        String[] parts = version.split("\\.");
        try {
            for (int i = 0; i < parts.length; i++) {
                Integer.parseInt(parts[i]);
            }
            return String.join(".", Stream.of(parts).map(s -> leftpad(s, 10)).toArray(String[]::new));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    static String leftpad(String s, int padTo) {
        while (s.length() < padTo) {
            s = " " + s;
        }
        return s;
    }

    static Path getTabNineDirectory() throws IOException {
        return Paths.get(System.getProperty("user.home"), ".tabnine");
    }
}
