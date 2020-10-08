package com.tabnine.binary;

import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import com.tabnine.binary.fetch.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.tabnine.general.StaticConfig.versionFullPath;
import static com.tabnine.testutils.TestData.*;
import static java.util.Collections.emptyList;
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
        when(binaryRemoteSource.existingLocalBetaVersion(VERSIONS_LIST)).thenReturn(Optional.of(new BinaryVersion(BETA_VERSION)));

        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(BETA_VERSION)));
    }

    @Test
    public void givenPreferredVersionAvailableLocallyWhenFetchBinaryThenLocalVersionIsReturned() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(versions(A_VERSION, ANOTHER_VERSION, PREFERRED_VERSION));
        when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.of(PREFERRED_VERSION));

        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(PREFERRED_VERSION)));
    }

    @Test
    public void givenPreferredVersionNotAvailableLocallyWhenThenVersionIsDownloadedAndPathIsReturned() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(versions(A_VERSION, ANOTHER_VERSION));
        when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.of(PREFERRED_VERSION));
        when(binaryDownloader.downloadBinary(PREFERRED_VERSION)).thenReturn(Optional.of(new BinaryVersion(PREFERRED_VERSION)));

        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(PREFERRED_VERSION)));
    }

    @Test
    public void givenFailedToFetchPreferredVersionWhenFetchBinaryThenReturnsLatestLocalVersion() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(VERSIONS_LIST);
        when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.empty());

        assertThat(binaryVersionFetcher.fetchBinary(), equalTo(versionFullPath(LATEST_VERSION)));
    }

    @Test
    public void givenFailedToFetchPreferredVersionAndNoLocalVersionWhenFetchBinaryThenExceptionRaised() throws Exception {
        when(localBinaryVersions.listExisting()).thenReturn(emptyList());
        when(binaryRemoteSource.fetchPreferredVersion()).thenReturn(Optional.empty());

        assertThrows(NoValidBinaryToRunException.class, binaryVersionFetcher::fetchBinary);
    }
}
