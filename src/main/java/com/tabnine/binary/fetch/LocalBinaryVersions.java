package com.tabnine.binary.fetch;

import com.intellij.util.text.SemVer;
import com.tabnine.StaticConfig;
import com.tabnine.binary.NoValidBinaryToRunException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LocalBinaryVersions {
    private BinaryValidator binaryValidator;

    public LocalBinaryVersions(BinaryValidator binaryValidator) {
        this.binaryValidator = binaryValidator;
    }

    @NotNull
    public List<String> listExisting() {
        File[] versionsFolders = Optional.ofNullable(StaticConfig.getBaseDirectory().toFile().listFiles()).orElse(new File[0]);

        return Stream.of(versionsFolders).map(File::getName).collect(toList());
    }

    public String getLatestValidVersion(List<String> versions) throws NoValidBinaryToRunException {
        return versions.stream().map(SemVer::parseFromText).filter(Objects::nonNull).sorted(Comparator.reverseOrder())
                .map(SemVer::toString).map(StaticConfig::versionFullPath).map(Path::toString)
                .filter(binaryValidator::isWorking)
                .findAny()
                .orElseThrow(NoValidBinaryToRunException::new);
    }
}
