package com.tabnine.healthCheck;

import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.statusBar.TabnineStatusBarWidget;
import com.tabnine.testUtils.HealthCheckTestUtils;
import com.tabnine.testUtils.MockBinaryResponse;
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnineCommon.capabilities.CapabilitiesService;
import com.tabnineCommon.general.ServiceLevel;
import com.tabnineCommon.general.StaticConfig;
import java.util.ArrayList;
import org.junit.Test;

public class StatusBarWidgetTests extends MockedBinaryCompletionTestCase {

  @Test
  public void should_get_connection_healthy_icon_when_connection_healthy()
      throws InterruptedException {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    HealthCheckTestUtils.notifyStateForWidget(
        ServiceLevel.FREE, true, CloudConnectionHealthStatus.Ok);
    MockBinaryResponse.mockCapabilities(binaryProcessGatewayMock, "API", new ArrayList<>());
    CapabilitiesService.getInstance().init();

    while (!CapabilitiesService.getInstance().isReady()) {
      Thread.sleep(100);
    }

    assertEquals(StaticConfig.ICON_AND_NAME_STARTER, widget.getIcon());
  }

  @Test
  public void should_get_connection_unhealthy_icon_when_connection_unhealthy()
      throws InterruptedException {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    HealthCheckTestUtils.notifyStateForWidget(
        ServiceLevel.FREE, true, CloudConnectionHealthStatus.Failed);
    MockBinaryResponse.mockCapabilities(binaryProcessGatewayMock, "API", new ArrayList<>());
    CapabilitiesService.getInstance().init();

    while (!CapabilitiesService.getInstance().isReady()) {
      Thread.sleep(100);
    }

    assertEquals(StaticConfig.ICON_AND_NAME_CONNECTION_LOST_STARTER, widget.getIcon());
  }
}
