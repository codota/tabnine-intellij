package com.tabnine.binary.fetch;

import com.tabnine.binary.FailedToDownloadException;
import com.tabnine.testutils.TestData;
import com.tabnine.testutils.WireMockExtension;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.tabnine.StaticConfig.REMOTE_CONNECTION_TIMEOUT;
import static com.tabnine.StaticConfig.REMOTE_VERSION_URL_PROPERTY;
import static com.tabnine.testutils.TestData.*;
import static com.tabnine.testutils.WireMockExtension.WIREMOCK_EXTENSION_DEFAULT_PORT;
import static java.lang.String.format;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(WireMockExtension.class)
@ExtendWith(MockitoExtension.class)
public class BinaryRemoteSourceTests {
    @InjectMocks
    private BinaryRemoteSource binaryRemoteSource;

    @BeforeEach
    public void setUp() {
        System.setProperty(REMOTE_VERSION_URL_PROPERTY, format("http://localhost:%d/", WIREMOCK_EXTENSION_DEFAULT_PORT));
    }

    @Test
    public void givenServerRespondingToVersionRequestWhenVersionRequestedThenItIsReturnProperly() throws FailedToDownloadException {
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody(PREFERRED_VERSION)));

        assertThat(binaryRemoteSource.fetchPreferredVersion(), Matchers.equalTo(PREFERRED_VERSION));
    }

    @Test
    public void givenNoServerToRespondToVersionRequestWhenVersionRequestedThenFailedToDownloadExceptionThrown() throws FailedToDownloadException {
        System.setProperty(REMOTE_VERSION_URL_PROPERTY, NONE_EXISTING_SERVICE);

        assertThrows(FailedToDownloadException.class, () -> binaryRemoteSource.fetchPreferredVersion());
    }

    @Test
    public void givenServerSlowingReallySlowToVersionRequestWhenVersionRequestedThenFailedToDownloadExceptionThrown() throws FailedToDownloadException {
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withFixedDelay(REMOTE_CONNECTION_TIMEOUT + EPSILON)
                        .withBody(PREFERRED_VERSION)));

        assertThrows(FailedToDownloadException.class, () -> binaryRemoteSource.fetchPreferredVersion());
    }
}
