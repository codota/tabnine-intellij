package com.tabnine.binary;

import com.tabnine.binary.fetch.BinaryDownloader;
import com.tabnine.binary.fetch.BinaryRemoteSource;
import com.tabnine.binary.fetch.BinaryVersionFetcher;
import com.tabnine.binary.fetch.LocalBinaryVersions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.tabnine.StaticConfig.versionFullPath;
import static com.tabnine.testutils.TestData.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BinaryVersionFetcherTests {
    @Mock
    private LocalBinaryVersions localBinaryVersions;
    @Mock
    private BinaryRemoteSource binaryRemoteSource;
    @Mock
    private BinaryDownloader binaryDownloader;

    @InjectMocks
    private BinaryVersionFetcher binaryVersionFetcher;

    @Test
    public void givenBetaVersionThatAvailableLocallyWhenFetchBinaryThenBetaVersionReturned() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(VERSIONS_LIST);
        when(binaryRemoteSource.existingLocalBetaVersion(VERSIONS_LIST)).thenReturn(BETA_VERSION);

        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(BETA_VERSION).toString()));
    }

    @Test
    public void givenPreferredVersionAvailableLocallyWhenFetchBinaryThenLocalVersionIsReturned() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(asList(A_VERSION, ANOTHER_VERSION, PREFERRED_VERSION));
        when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(PREFERRED_VERSION);

        String binaryPath = binaryVersionFetcher.fetchBinary();

        assertThat(binaryPath, equalTo(versionFullPath(PREFERRED_VERSION).toString()));
    }

    @Test
    public void givenPreferredVersionNotAvailableLocallyWhenThenVersionIsDownloadedAndPathIsReturned() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(asList(A_VERSION, ANOTHER_VERSION));
        when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(PREFERRED_VERSION);

        String binaryPath = binaryVersionFetcher.fetchBinary();

        verify(binaryDownloader).downloadBinary(PREFERRED_VERSION);
        assertThat(binaryPath, equalTo(versionFullPath(PREFERRED_VERSION).toString()));
    }

    @Test
    public void givenFailedToFetchPreferredVersionWhenFetchBinaryThenReturnsLatestLocalVersion() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(VERSIONS_LIST);
        when(binaryRemoteSource.fetchPreferredVersion()).thenThrow(FailedToDownloadException.class);
        when(localBinaryVersions.getLatestValidVersion(VERSIONS_LIST)).thenReturn(versionFullPath(LATEST_VERSION).toString());

        String binaryPath = binaryVersionFetcher.fetchBinary();

        verify(localBinaryVersions).getLatestValidVersion(VERSIONS_LIST);
        assertThat(binaryPath, equalTo(versionFullPath(LATEST_VERSION).toString()));
    }

    @Test
    public void givenFailedToFetchPreferredVersionAndNoLocalVersionWhenFetchBinaryThenExceptionRaised() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(VERSIONS_LIST);
        when(binaryRemoteSource.fetchPreferredVersion()).thenThrow(FailedToDownloadException.class);
        when(localBinaryVersions.getLatestValidVersion(VERSIONS_LIST)).thenThrow(NoValidBinaryToRunException.class);

        assertThrows(NoValidBinaryToRunException.class, () -> binaryVersionFetcher.fetchBinary());
    }
}
