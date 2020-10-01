package com.tabnine.binary.fetch;

import com.tabnine.StaticConfig;
import com.tabnine.binary.NoValidBinaryToRunException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.tabnine.StaticConfig.TABNINE_FOLDER_NAME;
import static com.tabnine.StaticConfig.USER_HOME_PATH_PROPERTY;
import static com.tabnine.testutils.TestData.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalBinaryVersionsTests {
    @TempDir
    public Path temporaryFolder;

    @Mock
    private BinaryValidator binaryValidator;
    @InjectMocks
    private LocalBinaryVersions localBinaryVersions;

    @BeforeEach
    public void setUp() {
        System.setProperty(USER_HOME_PATH_PROPERTY, temporaryFolder.toString());
    }

    @Test
    public void givenNoVersionsInTabNineBinaryFolderWhenListExistingThenResultIsEmptyList() throws Exception {
        Paths.get(temporaryFolder.toFile().toString(), TABNINE_FOLDER_NAME).toFile().mkdirs();

        assertThat(localBinaryVersions.listExisting(), equalTo(emptyList()));
    }

    @Test
    public void givenSomeVersionsInTabNineBinaryFolderWhenListExistingThenResultIsEmptyList(@TempDir Path temporaryFolder) throws Exception {
        Stream.of(A_VERSION, ANOTHER_VERSION).map(version -> Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, version).toFile()).forEach(File::mkdirs);


        assertThat(localBinaryVersions.listExisting(), containsInAnyOrder(A_VERSION, ANOTHER_VERSION));
    }

    @Test
    public void givenTheLatestLocalVersionIsWorkingWhenGetLatestValidVersionThenTheLatestReturned() throws Exception {
        when(binaryValidator.isWorking(any())).thenReturn(true);

        assertThat(localBinaryVersions.getLatestValidVersion(VERSIONS_LIST),
                equalTo(StaticConfig.versionFullPath(LATEST_VERSION).toString()));
    }

    @Test
    public void givenTheLatestLocalVersionIsInvalidWhenGetLatestValidVersionThenThePreviousVersionReturned() throws Exception {
        when(binaryValidator.isWorking(any())).thenReturn(true);
        when(binaryValidator.isWorking(contains(LATEST_VERSION))).thenReturn(false);

        assertThat(localBinaryVersions.getLatestValidVersion(VERSIONS_LIST),
                equalTo(StaticConfig.versionFullPath(PRELATEST_VERSION).toString()));
    }

    @Test
    public void givenAllLocalVersionsAreInvalidWhenGetLatestValidVersionThenNoValidBinaryToRunExceptionThrown() throws Exception {
        when(binaryValidator.isWorking(any())).thenReturn(false);

        Assertions.assertThrows(NoValidBinaryToRunException.class, () -> localBinaryVersions.getLatestValidVersion(VERSIONS_LIST));
    }
}
