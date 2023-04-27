package com.tabnine.binary;

import static com.tabnineCommon.general.StaticConfig.versionFullPath;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.tabnine.testUtils.TestData;
import com.tabnineCommon.binary.exceptions.InvalidVersionPathException;
import com.tabnineCommon.binary.fetch.*;
import java.util.List;
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
public class BootstrapperSupportTests {
  @Mock private LocalBinaryVersions localBinaryVersions;
  @Mock private BinaryRemoteSource binaryRemoteSource;
  @Mock private BundleDownloader bundleDownloader;

  @InjectMocks private BinaryVersionFetcher binaryVersionFetcher;

  @BeforeEach
  public void setUp() throws BackingStoreException {
    Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
    preferences.clear();
  }

  @Test
  public void testWhenBootstrapperVersionIsNotLocalItWillDownloadIt() throws Exception {
    when(binaryRemoteSource.fetchPreferredVersion(anyString())).thenReturn(Optional.of("9.9.9"));
    when(bundleDownloader.downloadAndExtractBundle("9.9.9", null))
        .thenReturn(Optional.of(new BinaryVersion("9.9.9")));
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("9.9.9")));
  }

  @Test
  public void testWhenBootstrapperVersionExistsItWillUseIt() throws Exception {
    Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
    preferences.put(BootstrapperSupport.BOOTSTRAPPED_VERSION_KEY, "8.8.8");
    List<BinaryVersion> binaryVersions = TestData.aVersions();
    binaryVersions.add(new BinaryVersion("8.8.8"));
    when(localBinaryVersions.listExisting()).thenReturn(binaryVersions);
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("8.8.8")));
  }

  @Test
  public void testWhenActiveVersionExistsItWillUseIt() throws Exception {
    Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
    preferences.put(BootstrapperSupport.BOOTSTRAPPED_VERSION_KEY, "8.8.8");
    when(localBinaryVersions.activeVersion()).thenReturn(Optional.of(new BinaryVersion("7.8.9")));
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("7.8.9")));
  }

  @Test
  public void testWhenGreaterVersionThanPreferedBootstrapperVersionExistsItWillBeUsedInstead()
      throws Exception {
    Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
    preferences.put(BootstrapperSupport.BOOTSTRAPPED_VERSION_KEY, "5.5.5");
    List<BinaryVersion> binaryVersions = TestData.aVersions();
    binaryVersions.add(new BinaryVersion("55.55.55"));
    when(localBinaryVersions.listExisting()).thenReturn(binaryVersions);
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("55.55.55")));
  }

  @Test
  public void testWhenBootstrappedVersionDidNotExistLocallyTheBootstrapperWillDownloadAgain()
      throws Exception {
    Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
    preferences.put(BootstrapperSupport.BOOTSTRAPPED_VERSION_KEY, "6.6.6");
    when(localBinaryVersions.listExisting()).thenReturn(TestData.aVersions());
    when(binaryRemoteSource.fetchPreferredVersion(anyString())).thenReturn(Optional.of("66.66.66"));
    when(bundleDownloader.downloadAndExtractBundle("66.66.66", null))
        .thenReturn(Optional.of(new BinaryVersion("66.66.66")));
    assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("66.66.66")));
  }

  @Test
  public void testWhenBootstrappedVersionFailedToDownloadWillFallbackToLocalVersion()
      throws Exception {
    when(localBinaryVersions.listExisting()).thenReturn(TestData.aVersions());
    when(binaryRemoteSource.fetchPreferredVersion(anyString())).thenReturn(Optional.of("7.7.7"));
    when(binaryRemoteSource.fetchPreferredVersion())
        .thenReturn(Optional.of(TestData.PREFERRED_VERSION));
    when(bundleDownloader.downloadAndExtractBundle("7.7.7", null)).thenReturn(Optional.empty());
    assertThat(
        binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(TestData.PREFERRED_VERSION)));
  }

  @Test
  public void whenVersionIsInvalidThenVersionFullPathFails() {
    assertThrows(InvalidVersionPathException.class, () -> versionFullPath("not a semver version"));
  }
}
