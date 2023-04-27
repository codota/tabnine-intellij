package com.tabnine.binary.fetch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.tabnineCommon.general.StaticConfig.*;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tabnine.testUtils.TabnineMatchers;
import com.tabnine.testUtils.TestData;
import com.tabnine.testUtils.WireMockExtension;
import com.tabnineCommon.binary.fetch.BinaryDownloader;
import com.tabnineCommon.binary.fetch.GeneralDownloader;
import com.tabnineCommon.binary.fetch.TempBinaryValidator;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(WireMockExtension.class)
@ExtendWith(MockitoExtension.class)
public class BinaryDownloaderTests {
  @TempDir public Path temporaryFolder;
  @Mock private TempBinaryValidator tempBinaryValidator;
  @Spy private GeneralDownloader downloader = new GeneralDownloader();
  @InjectMocks private BinaryDownloader binaryDownloader;

  private String originalHome = System.getProperty(USER_HOME_PATH_PROPERTY);
;
  @BeforeEach
  public void setUp() {
    System.setProperty(USER_HOME_PATH_PROPERTY, temporaryFolder.toString());
//    System.setProperty(
//        REMOTE_BASE_URL_PROPERTY,
//        format("https://localhost:%d", WireMockExtension.WIREMOCK_EXTENSION_DEFAULT_PORT));

    Paths.get(temporaryFolder.toFile().toString(), TABNINE_FOLDER_NAME).toFile().mkdirs();
  }

  @AfterEach
  public void tearDown() throws Exception {
    System.setProperty(USER_HOME_PATH_PROPERTY, originalHome);
  }

  @Test
  public void whenDownloadingBinarySuccessfullyThenItsContentIsWrittenSuccessfullyToTemporaryFile() {
    stubFor(
        get(urlPathEqualTo(String.join("/", "", TestData.A_VERSION, TARGET_NAME, EXECUTABLE_NAME)))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody(TestData.A_BINARY_CONTENT)));

    binaryDownloader.downloadBinary(TestData.A_VERSION, TestData.A_SERvER_URL);

    File[] files = Paths.get(versionFullPath(TestData.A_VERSION)).getParent().toFile().listFiles();
    assertThat(files, arrayWithSize(1));
    assertThat(
        files,
        Matchers.arrayContaining(TabnineMatchers.fileContentEquals(TestData.A_BINARY_CONTENT)));
  }

  @Test
  public void whenDownloadingBinarySuccessfullyThenValidatorCalledWithIt() throws Exception {
    stubFor(
        get(urlEqualTo(String.join("/", TestData.A_SERvER_URL, TestData.A_VERSION, TARGET_NAME, EXECUTABLE_NAME)))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody(TestData.A_BINARY_CONTENT)));

    binaryDownloader.downloadBinary(TestData.A_VERSION, TestData.A_SERvER_URL);

    verify(tempBinaryValidator)
        .validateAndRename(
            TabnineMatchers.pathStartsWith(versionFullPath(TestData.A_VERSION) + ".download"),
            eq(Paths.get(versionFullPath(TestData.A_VERSION))));
  }

  @Test
  public void givenServerResultInErrorWhenDownloadingBinaryThenFailedToDownloadExceptionThrown()
      throws Exception {
    stubFor(
        get(urlPathEqualTo(String.join("/", "", TestData.A_VERSION, TARGET_NAME, EXECUTABLE_NAME)))
            .willReturn(aResponse().withStatus(TestData.INTERNAL_SERVER_ERROR)));

    assertThat(
        binaryDownloader.downloadBinary(TestData.A_VERSION, TestData.A_SERvER_URL),
        Matchers.equalTo(Optional.empty()));
  }

  @Test
  public void givenNoServerResultWhenDownloadingBinaryThenFailedToDownloadExceptionThrown() {
    assertThat(
        binaryDownloader.downloadBinary(TestData.A_VERSION, TestData.A_SERvER_URL),
        Matchers.equalTo(Optional.empty()));
  }
}
