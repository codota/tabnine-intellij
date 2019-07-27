import com.intellij.openapi.util.SystemInfo;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

class TabNineFinder {
    static final String CDN_URL = "https://update.tabnine.com";

    static String getTabNinePath() throws IOException {
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
        sortBySemverDescending(children);
        String foundPath = searchForTabNine(children);
        if (foundPath != null) {
            return foundPath;
        }
        return downloadTabNine();
    }

    static String downloadTabNine() throws IOException {
        String version = new String(download(CDN_URL + "/version")).trim();
        String url = String.join("/", new String[]{CDN_URL, version, getTargetName(), getExeName()});
        byte[] exe = download(url);
        if (exe.length < 1000 * 1000) {
            throw new IOException("Couldn't get TabNine from " + url);
        }
        Path dst = Paths.get(getTabNineDirectory().toString(), version, getTargetName(), getExeName());
        writeBytes(exe, dst);
        if (!dst.toFile().setExecutable(true)) {
            throw new IOException("Could not set execute permission on " + dst.toString());
        }
        return dst.toString();
    }

    static void writeBytes(byte[] b, Path dst) throws IOException {
        dst.getParent().toFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(dst.toString());
        fos.write(b);
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

    static void sortBySemverDescending(String[] versions) {
        Arrays.sort(versions, (a, b) -> parseSemver(b).compareTo(parseSemver(a)));
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
