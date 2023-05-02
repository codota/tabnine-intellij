package com.tabnine.binary;

import static com.tabnineCommon.general.StaticConfig.versionFullPath;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.tabnine.testUtils.TestData;
import com.tabnineCommon.binary.exceptions.NoValidBinaryToRunException;
import com.tabnineCommon.binary.fetch.*;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BinaryVersionFetcherTests {
  @Mock private LocalBinaryVersions localBinaryVersions;
  @Mock private BinaryRemoteSource binaryRemoteSource;
  @Mock private BinaryDownloader binaryDownloader;
  @Mock private BundleDownloader bundleDownloader;
  @InjectMocks private BinaryVersionFetcher binaryVersionFetcher;

  @BeforeEach
  public void setUp() throws BackingStoreException {
    Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
    preferences.clear();
    when(binaryRemoteSource.fetchPreferredVersion(anyString()))
        .thenReturn(Optional.of(TestData.PREFERRED_VERSION));
  }

  @Test
  public void givenBetaVersionThatAvailableLocallyWhenFetchBinaryThenBetaVersionReturned()
      throws Exception {
    when(localBinaryVersions.listExisting()).thenReturn(TestData.aVersions());
    when(binaryRemoteSource.existingLocalBetaVersion(TestData.aVersions()))
        .thenReturn(Optional.of(new BinaryVersion(TestData.BETA_VERSION)));
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(TestData.BETA_VERSION)));
  }

  @Test
  public void givenPreferredVersionAvailableLocallyWhenFetchBinaryThenLocalVersionIsReturned()
      throws Exception {
    when(localBinaryVersions.listExisting())
        .thenReturn(
            TestData.versions(
                TestData.A_VERSION, TestData.ANOTHER_VERSION, TestData.PREFERRED_VERSION));
    when(binaryRemoteSource.fetchPreferredVersion())
        .thenReturn(Optional.of(TestData.PREFERRED_VERSION));
    assertThat(
        binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(TestData.PREFERRED_VERSION)));
  }

  @Test
  public void givenPreferredVersionNotAvailableLocallyWhenThenVersionIsDownloadedAndPathIsReturned()
      throws Exception {
    when(localBinaryVersions.listExisting())
        .thenReturn(TestData.versions(TestData.A_VERSION, TestData.ANOTHER_VERSION));
    when(binaryRemoteSource.fetchPreferredVersion())
        .thenReturn(Optional.of(TestData.PREFERRED_VERSION));
    when(binaryDownloader.downloadBinary(TestData.PREFERRED_VERSION))
        .thenReturn(Optional.of(new BinaryVersion(TestData.PREFERRED_VERSION)));
    assertThat(
        binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(TestData.PREFERRED_VERSION)));
  }

  @Test
  public void givenFailedToFetchPreferredVersionWhenFetchBinaryThenReturnsLatestLocalVersion()
      throws Exception {
    when(localBinaryVersions.listExisting()).thenReturn(TestData.aVersions());
    when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.empty());
    assertThat(
        binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(TestData.LATEST_VERSION)));
  }

  @Test
  public void
      givenFailedToFetchPreferredVersionAndNoLocalVersionWhenFetchBinaryThenExceptionRaised()
          throws Exception {
    when(localBinaryVersions.listExisting()).thenReturn(emptyList());
    when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.empty());
    assertThrows(NoValidBinaryToRunException.class, binaryVersionFetcher::fetchBinary);
  }
}
