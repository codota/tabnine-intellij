package com.tabnine.binary.fetch;

import com.tabnine.binary.FailedToDownloadException;
import com.tabnine.testutils.TabnineMatchers;
import com.tabnine.testutils.TestData;
import com.tabnine.testutils.WireMockExtension;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.tabnine.StaticConfig.*;
import static com.tabnine.testutils.TabnineMatchers.fileContentEquals;
import static com.tabnine.testutils.TestData.*;
import static java.lang.String.format;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(WireMockExtension.class)
@ExtendWith(MockitoExtension.class)
public class BinaryDownloaderTests {
    @TempDir
    public Path temporaryFolder;
    @Mock
    private TempBinaryValidator tempBinaryValidator;
    @InjectMocks
    private BinaryDownloader binaryDownloader;

    @BeforeEach
    public void setUp() {
        System.setProperty(USER_HOME_PATH_PROPERTY, temporaryFolder.toString());
        System.setProperty(REMOTE_BASE_URL_PROPERTY, format("http://localhost:%d", WireMockExtension.WIREMOCK_EXTENSION_DEFAULT_PORT));

        Paths.get(temporaryFolder.toFile().toString(), TABNINE_FOLDER_NAME).toFile().mkdirs();
    }

    @Test
    public void whenDownloadingBinarySuccessfullyThenItsContentIsWrittenSuccessfullyToTemporaryFile() throws Exception {
        stubFor(get(urlEqualTo(String.join("/", "", A_VERSION, TARGET_NAME, EXECUTABLE_NAME)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody(A_BINARY_CONTENT)));

        binaryDownloader.downloadBinary(A_VERSION);

        File[] files = versionFullPath(A_VERSION).getParent().toFile().listFiles();
        assertThat(files, arrayWithSize(1));
        assertThat(files, arrayContaining(fileContentEquals(A_BINARY_CONTENT)));
    }

    @Test
    public void whenDownloadingBinarySuccessfullyThenValidatorCalledWithIt() throws Exception {
        stubFor(get(urlEqualTo(String.join("/", "", A_VERSION, TARGET_NAME, EXECUTABLE_NAME)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody(A_BINARY_CONTENT)));

        binaryDownloader.downloadBinary(A_VERSION);

        verify(tempBinaryValidator).validateAndRename(TabnineMatchers.pathStartsWith(versionFullPath(A_VERSION).toString() + ".download"), eq(versionFullPath(A_VERSION)));
    }

    @Test
    public void givenServerResultInErrorWhenDownloadingBinaryThenFailedToDownloadExceptionThrown() throws Exception {
        stubFor(get(urlEqualTo(String.join("/", "", A_VERSION, TARGET_NAME, EXECUTABLE_NAME)))
                .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR)));

        assertThrows(FailedToDownloadException.class, () -> binaryDownloader.downloadBinary(A_VERSION));
    }

    @Test
    public void givenNoServerResultWhenDownloadingBinaryThenFailedToDownloadExceptionThrown() throws Exception {
        assertThrows(FailedToDownloadException.class, () -> binaryDownloader.downloadBinary(A_VERSION));
    }
}
