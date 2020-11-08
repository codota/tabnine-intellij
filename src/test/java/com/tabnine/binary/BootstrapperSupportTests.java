package com.tabnine.binary;
import com.tabnine.binary.fetch.*;
import com.tabnine.general.StaticConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static com.tabnine.general.StaticConfig.versionFullPath;
import static com.tabnine.testutils.TestData.PREFERRED_VERSION;
import static com.tabnine.testutils.TestData.aVersions;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BootstrapperSupportTests {
    @Mock
    private LocalBinaryVersions localBinaryVersions;
    @Mock
    private BinaryRemoteSource binaryRemoteSource;
    @Mock
    private BundleDownloader bundleDownloader;

    @InjectMocks
    private BinaryVersionFetcher binaryVersionFetcher;

    @BeforeEach
    public void setUp() throws BackingStoreException {
        Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
        preferences.clear();
    }

    @Test
    public void testWhenBootstrapperVersionIsNotLocalItWillDownloadIt() throws Exception {
        when(binaryRemoteSource.fetchPreferredVersion(StaticConfig.getTabNineBundleVersionUrl())).thenReturn(Optional.of("9.9.9"));
        when(bundleDownloader.downloadAndExtractBundle("9.9.9")).thenReturn(Optional.of(new BinaryVersion("9.9.9")));
        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("9.9.9")));
    }
    @Test
    public void testWhenBootstrapperVersionExistsItWillUseIt() throws Exception {
        Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
        preferences.put("bootstrapped version","8.8.8");
        List<BinaryVersion> binaryVersions = aVersions();
        binaryVersions.add(new BinaryVersion("8.8.8"));
        when(localBinaryVersions.listExisting()).thenReturn(binaryVersions);
        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("8.8.8")));
    }
    @Test
    public void testWhenGreaterVersionThanPreferedBootstrapperVersionExistsItWillBeUsedInstead() throws Exception {
        Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
        preferences.put("bootstrapped version","5.5.5");
        List<BinaryVersion> binaryVersions = aVersions();
        binaryVersions.add(new BinaryVersion("55.55.55"));
        when(localBinaryVersions.listExisting()).thenReturn(binaryVersions);
        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("55.55.55")));
    }
    @Test
    public void testWhenBootstrappedVersionDidNotExistLocallyTheBootstrapperWillDownloadAgain() throws Exception {
        Preferences preferences = Preferences.userNodeForPackage(BootstrapperSupport.class);
        preferences.put("bootstrapped version","6.6.6");
        when(localBinaryVersions.listExisting()).thenReturn(aVersions());
        when(binaryRemoteSource.fetchPreferredVersion(StaticConfig.getTabNineBundleVersionUrl())).thenReturn(Optional.of("66.66.66"));
        when(bundleDownloader.downloadAndExtractBundle("66.66.66")).thenReturn(Optional.of(new BinaryVersion("66.66.66")));
        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath("66.66.66")));
    }
    @Test
    public void testWhenBootstrappedVersionFailedToDownloadWillFallbackToLocalVersion() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(aVersions());
        when(binaryRemoteSource.fetchPreferredVersion(StaticConfig.getTabNineBundleVersionUrl())).thenReturn(Optional.of("7.7.7"));
        when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.of(PREFERRED_VERSION));
        when(bundleDownloader.downloadAndExtractBundle("7.7.7")).thenReturn(Optional.empty());
        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(PREFERRED_VERSION)));
    }
}
