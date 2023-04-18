package com.tabnineCommon.testUtils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * To have multiple instances, use it like so:
 *
 * <pre>{@code
 * @RegisterExtension
 * public WireMockExtension mockServer1 = new WireMockExtension(8081);
 *
 * @RegisterExtension
 * public WireMockExtension mockServer2 = new WireMockExtension(8082);
 * }</pre>
 */
public class WireMockExtension extends WireMockServer
    implements BeforeEachCallback, AfterEachCallback {
  public static final int WIREMOCK_EXTENSION_DEFAULT_PORT =
      WireMockConfiguration.options().portNumber();

  public WireMockExtension() {
    super(WIREMOCK_EXTENSION_DEFAULT_PORT);
  }

  public WireMockExtension(int port) {
    super(port);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    this.start();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    this.stop();
    this.resetAll();
  }
}
