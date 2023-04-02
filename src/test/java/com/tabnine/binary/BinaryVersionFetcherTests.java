package com.tabnine.binary;

import static com.tabnine.general.StaticConfig.versionFullPath;
import static com.tabnine.testUtils.TestData.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import com.tabnine.binary.fetch.*;
import com.tabnine.general.StaticConfig;
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
  public void setUp() throws BackingStoreException, NoValidBinaryToRunException {
    Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
    preferences.clear();
    when(binaryRemoteSource.fetchPreferredVersion(anyString()))
        .thenReturn(Optional.of(PREFERRED_VERSION));
  }

  @Test
  public void givenBetaVersionThatAvailableLocallyWhenFetchBinaryThenBetaVersionReturned()
      throws Exception {
    when(localBinaryVersions.listExisting()).thenReturn(aVersions());
    when(binaryRemoteSource.existingLocalBetaVersion(aVersions()))
        .thenReturn(Optional.of(new BinaryVersion(BETA_VERSION)));
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(BETA_VERSION)));
  }

  @Test
  public void givenPreferredVersionAvailableLocallyWhenFetchBinaryThenLocalVersionIsReturned()
      throws Exception {
    when(localBinaryVersions.listExisting())
        .thenReturn(versions(A_VERSION, ANOTHER_VERSION, PREFERRED_VERSION));
    when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.of(PREFERRED_VERSION));
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(PREFERRED_VERSION)));
  }

  @Test
  public void givenPreferredVersionNotAvailableLocallyWhenThenVersionIsDownloadedAndPathIsReturned()
      throws Exception {
    when(localBinaryVersions.listExisting()).thenReturn(versions(A_VERSION, ANOTHER_VERSION));
    when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.of(PREFERRED_VERSION));
    when(binaryDownloader.downloadBinary(PREFERRED_VERSION))
        .thenReturn(Optional.of(new BinaryVersion(PREFERRED_VERSION)));
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(PREFERRED_VERSION)));
  }

  @Test
  public void givenFailedToFetchPreferredVersionWhenFetchBinaryThenReturnsLatestLocalVersion()
      throws Exception {
    when(localBinaryVersions.listExisting()).thenReturn(aVersions());
    when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.empty());
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(LATEST_VERSION)));
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
