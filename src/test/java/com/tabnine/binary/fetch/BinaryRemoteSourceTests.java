package com.tabnine.binary.fetch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.tabnine.general.StaticConfig.*;
import static com.tabnine.testUtils.TabnineMatchers.emptyOptional;
import static com.tabnine.testUtils.TabnineMatchers.versionMatch;
import static com.tabnine.testUtils.TestData.*;
import static com.tabnine.testUtils.WireMockExtension.WIREMOCK_EXTENSION_DEFAULT_PORT;
import static java.lang.String.format;
import static org.junit.Assert.assertThat;

import com.intellij.mock.MockApplication;
import com.intellij.openapi.Disposable;
import com.tabnine.binary.exceptions.FailedToDownloadException;
import com.tabnine.testUtils.WireMockExtension;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(WireMockExtension.class)
@ExtendWith(MockitoExtension.class)
public class BinaryRemoteSourceTests implements Disposable {
  @InjectMocks private BinaryRemoteSource binaryRemoteSource;

  @BeforeEach
  public void setUp() {
    System.setProperty(
        REMOTE_VERSION_URL_PROPERTY,
        format("http://localhost:%d/", WIREMOCK_EXTENSION_DEFAULT_PORT));
    System.setProperty(
        REMOTE_BETA_VERSION_URL_PROPERTY,
        format("http://localhost:%d/", WIREMOCK_EXTENSION_DEFAULT_PORT));
    MockApplication.setUp(this);
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

  @Override
  public void dispose() {}
}
