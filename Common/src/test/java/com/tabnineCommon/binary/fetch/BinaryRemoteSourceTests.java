package com.tabnineCommon.binary.fetch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.tabnineCommon.general.StaticConfig.*;
import static com.tabnineCommon.testUtils.TabnineMatchers.emptyOptional;
import static com.tabnineCommon.testUtils.TabnineMatchers.versionMatch;
import static com.tabnineCommon.testUtils.TestData.*;
import static com.tabnineCommon.testUtils.WireMockExtension.WIREMOCK_EXTENSION_DEFAULT_PORT;
import static java.lang.String.format;
import static org.junit.Assert.assertThat;

import com.tabnineCommon.binary.exceptions.FailedToDownloadException;
import com.tabnineCommon.testUtils.WireMockExtension;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(WireMockExtension.class)
@ExtendWith(MockitoExtension.class)
public class BinaryRemoteSourceTests {
  @InjectMocks private BinaryRemoteSource binaryRemoteSource;

  @BeforeEach
  public void setUp() {
    System.setProperty(
        REMOTE_VERSION_URL_PROPERTY,
        format("http://localhost:%d/", WIREMOCK_EXTENSION_DEFAULT_PORT));
    System.setProperty(
        REMOTE_BETA_VERSION_URL_PROPERTY,
        format("http://localhost:%d/", WIREMOCK_EXTENSION_DEFAULT_PORT));
  }

  @Test
  public void givenServerResponseWhenVersionRequestedThenItIsReturnProperly()
      throws FailedToDownloadException {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse().withHeader("Content-Type", "text/plain").withBody(PREFERRED_VERSION)));

    assertThat(
        binaryRemoteSource.fetchPreferredVersion(),
        Matchers.equalTo(Optional.of(PREFERRED_VERSION)));
  }

  @Test
  public void givenNoServerToRespondWhenVersionRequestedThenFailedToDownloadExceptionThrown()
      throws FailedToDownloadException {
    System.setProperty(REMOTE_VERSION_URL_PROPERTY, NONE_EXISTING_SERVICE);

    assertThat(binaryRemoteSource.fetchPreferredVersion(), emptyOptional());
  }

  @Test
  public void givenServerResponseLateWhenVersionRequestedThenFailedToDownloadExceptionThrown()
      throws FailedToDownloadException {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withFixedDelay(REMOTE_CONNECTION_TIMEOUT + EPSILON)
                    .withBody(PREFERRED_VERSION)));

    assertThat(binaryRemoteSource.fetchPreferredVersion(), emptyOptional());
  }

  @Test
  public void givenServerResponseWhenBetaVersionRequestedAndIsAvailableLocallyThenItIsReturned()
      throws Exception {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse().withHeader("Content-Type", "text/plain").withBody(BETA_VERSION)));

    assertThat(
        binaryRemoteSource.existingLocalBetaVersion(versionsWithBeta()),
        versionMatch(BETA_VERSION));
  }

  @Test
  public void givenServerResponseWhenBetaVersionRequestedAndIsNotAvailableLocallyThenNullReturned()
      throws Exception {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse().withHeader("Content-Type", "text/plain").withBody(BETA_VERSION)));

    assertThat(binaryRemoteSource.existingLocalBetaVersion(aVersions()), emptyOptional());
  }

  @Test
  public void givenNoServerToRespondToVersionRequestWhenVersionRequestedThenNullReturned()
      throws Exception {
    System.setProperty(REMOTE_VERSION_URL_PROPERTY, NONE_EXISTING_SERVICE);

    assertThat(binaryRemoteSource.existingLocalBetaVersion(aVersions()), emptyOptional());
  }

  @Test
  public void givenServerResponseLateWhenBetaVersionRequestedThenNullReturned() throws Exception {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withFixedDelay(REMOTE_CONNECTION_TIMEOUT + EPSILON)
                    .withBody(BETA_VERSION)));

    assertThat(binaryRemoteSource.existingLocalBetaVersion(aVersions()), emptyOptional());
  }
}
