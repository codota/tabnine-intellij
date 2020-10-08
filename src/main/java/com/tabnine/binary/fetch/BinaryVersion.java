package com.tabnine.binary.fetch;

import java.util.Objects;

import static com.tabnine.general.StaticConfig.versionFullPath;

public class BinaryVersion {
    private final String versionFullPath;
    private final String version;

    public BinaryVersion(String versionFullPath, String version) {
        this.versionFullPath = versionFullPath;
        this.version = version;
    }

    public BinaryVersion(String version) {
        this(versionFullPath(version), version);
    }

    public String getVersionFullPath() {
        return versionFullPath;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "BinaryVersions{" +
                "versionFullPath='" + versionFullPath + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryVersion that = (BinaryVersion) o;
        return Objects.equals(versionFullPath, that.versionFullPath) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionFullPath, version);
    }
}
