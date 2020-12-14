package com.tabnine.binary.fetch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tabnine.general.StaticConfig.*;
import static com.tabnine.testutils.TestData.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalBinaryVersionsTests {
    @TempDir
    public Path temporaryFolder;
    @Mock
    private BinaryValidator binaryValidator;
    @InjectMocks
    private LocalBinaryVersions localBinaryVersions;

    private String originalHome = System.getProperty(USER_HOME_PATH_PROPERTY);

    @BeforeEach
    public void setUp() {
        System.setProperty(USER_HOME_PATH_PROPERTY, temporaryFolder.toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        System.setProperty(USER_HOME_PATH_PROPERTY, originalHome);
    }

    @Test
    public void givenNoVersionsInTabNineBinaryFolderWhenListExistingThenResultIsEmptyList() throws Exception {
        Paths.get(temporaryFolder.toFile().toString(), TABNINE_FOLDER_NAME).toFile().mkdirs();

        assertThat(localBinaryVersions.listExisting(), equalTo(emptyList()));
    }

    @Test
    public void givenSomeValidLocalVersionsWhenListExistingVersionsThenResultContainsSortedValidVersions() throws Exception {
        when(binaryValidator.isWorking(versionFullPath(PRELATEST_VERSION))).thenReturn(true);
        when(binaryValidator.isWorking(versionFullPath(LATEST_VERSION))).thenReturn(true);
        when(binaryValidator.isWorking(versionFullPath(A_VERSION))).thenReturn(true);
        when(binaryValidator.isWorking(versionFullPath(ANOTHER_VERSION))).thenReturn(false);
        Stream.of(ANOTHER_VERSION, PRELATEST_VERSION, LATEST_VERSION, A_VERSION).map(version -> Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, version).toFile()).forEach(File::mkdirs);


        assertThat(localBinaryVersions.listExisting(), equalTo(versions(LATEST_VERSION, PRELATEST_VERSION, A_VERSION)));
    }

    @Test
    public void givenAllLocalVersionsInvalidWhenListExistingVersionsThenResultIsEmptyList() throws Exception {
        when(binaryValidator.isWorking(Mockito.any())).thenReturn(false);
        Stream.of(A_VERSION, ANOTHER_VERSION).map(version -> Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, version).toFile()).forEach(File::mkdirs);


        assertThat(localBinaryVersions.listExisting(), empty());
    }

    @Test
    public void givenNoActiveFileThenReturnNone() {
        assertThat(localBinaryVersions.activeVersion(), equalTo(Optional.empty()));
    }

    @Test
    public void givenActiveFileWithNoLinesThenReturnNone() throws IOException {
        Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME).toFile().mkdirs();
        Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, ".active").toFile().createNewFile();
        assertThat(localBinaryVersions.activeVersion(), equalTo(Optional.empty()));
    }

    @Test
    public void givenActiveFileWithInvalidVersionThenReturnNone() throws IOException {
        Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME).toFile().mkdirs();
        Files.write(Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, ".active"),  Stream.of(PREFERRED_VERSION).collect(Collectors.toList()));

        when(binaryValidator.isWorking(versionFullPath(PREFERRED_VERSION))).thenReturn(false);

        assertThat(localBinaryVersions.activeVersion(), equalTo(Optional.empty()));
    }

    @Test
    public void givenActiveFileWithValidVersionThenReturnVersion() throws IOException {
        Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME).toFile().mkdirs();
        Files.write(Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, ".active"),  Stream.of(PREFERRED_VERSION).collect(Collectors.toList()));

        when(binaryValidator.isWorking(versionFullPath(PREFERRED_VERSION))).thenReturn(true);

        assertThat(localBinaryVersions.activeVersion(), equalTo(Optional.of(new BinaryVersion(PREFERRED_VERSION))));
    }

}
