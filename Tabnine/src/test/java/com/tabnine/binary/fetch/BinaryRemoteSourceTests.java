package com.tabnine.binary.fetch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.tabnineCommon.general.StaticConfig.*;
import static org.junit.Assert.assertThat;

import com.tabnine.testUtils.TabnineMatchers;
import com.tabnine.testUtils.TestData;
import com.tabnine.testUtils.WireMockExtension;
import com.tabnineCommon.binary.exceptions.FailedToDownloadException;
import com.tabnineCommon.binary.fetch.BinaryRemoteSource;
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
        String.format("http://localhost:%d/", WireMockExtension.WIREMOCK_EXTENSION_DEFAULT_PORT));
    System.setProperty(
        REMOTE_BETA_VERSION_URL_PROPERTY,
        String.format("http://localhost:%d/", WireMockExtension.WIREMOCK_EXTENSION_DEFAULT_PORT));
  }

  @Test
  public void givenServerResponseWhenVersionRequestedThenItIsReturnProperly()
      throws FailedToDownloadException {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody(TestData.PREFERRED_VERSION)));

    assertThat(
        binaryRemoteSource.fetchPreferredVersion(),
        Matchers.equalTo(Optional.of(TestData.PREFERRED_VERSION)));
  }

  @Test
  public void givenNoServerToRespondWhenVersionRequestedThenFailedToDownloadExceptionThrown()
      throws FailedToDownloadException {
    System.setProperty(REMOTE_VERSION_URL_PROPERTY, TestData.NONE_EXISTING_SERVICE);

    assertThat(binaryRemoteSource.fetchPreferredVersion(), TabnineMatchers.emptyOptional());
  }

  @Test
  public void givenServerResponseLateWhenVersionRequestedThenFailedToDownloadExceptionThrown()
      throws FailedToDownloadException {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withFixedDelay(REMOTE_CONNECTION_TIMEOUT + TestData.EPSILON)
                    .withBody(TestData.PREFERRED_VERSION)));

    assertThat(binaryRemoteSource.fetchPreferredVersion(), TabnineMatchers.emptyOptional());
  }

  @Test
  public void givenServerResponseWhenBetaVersionRequestedAndIsAvailableLocallyThenItIsReturned()
      throws Exception {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody(TestData.BETA_VERSION)));

    assertThat(
        binaryRemoteSource.existingLocalBetaVersion(TestData.versionsWithBeta()),
        TabnineMatchers.versionMatch(TestData.BETA_VERSION));
  }

  @Test
  public void givenServerResponseWhenBetaVersionRequestedAndIsNotAvailableLocallyThenNullReturned()
      throws Exception {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody(TestData.BETA_VERSION)));

    assertThat(
        binaryRemoteSource.existingLocalBetaVersion(TestData.aVersions()),
        TabnineMatchers.emptyOptional());
  }

  @Test
  public void givenNoServerToRespondToVersionRequestWhenVersionRequestedThenNullReturned()
      throws Exception {
    System.setProperty(REMOTE_VERSION_URL_PROPERTY, TestData.NONE_EXISTING_SERVICE);

    assertThat(
        binaryRemoteSource.existingLocalBetaVersion(TestData.aVersions()),
        TabnineMatchers.emptyOptional());
  }

  @Test
  public void givenServerResponseLateWhenBetaVersionRequestedThenNullReturned() throws Exception {
    stubFor(
        get(urlEqualTo("/"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withFixedDelay(REMOTE_CONNECTION_TIMEOUT + TestData.EPSILON)
                    .withBody(TestData.BETA_VERSION)));

    assertThat(
        binaryRemoteSource.existingLocalBetaVersion(TestData.aVersions()),
        TabnineMatchers.emptyOptional());
  }
}
