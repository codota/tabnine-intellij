package com.tabnine.binary.fetch;

import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.tabnine.StaticConfig.getBaseDirectory;
import static com.tabnine.StaticConfig.versionFullPath;
import static java.util.stream.Collectors.toList;

public class LocalBinaryVersions {
    private BinaryValidator binaryValidator;

    public LocalBinaryVersions(BinaryValidator binaryValidator) {
        this.binaryValidator = binaryValidator;
    }

    @NotNull
    public List<BinaryVersion> listExisting() {
        File[] versionsFolders = Optional.ofNullable(getBaseDirectory().toFile().listFiles()).orElse(new File[0]);

        return Stream.of(versionsFolders).map(File::getName).map(SemVer::parseFromText).filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .map(SemVer::toString)
                .map(BinaryVersion::new)
                .filter(version -> binaryValidator.isWorking(version.getVersionFullPath())).collect(toList());
    }
}
