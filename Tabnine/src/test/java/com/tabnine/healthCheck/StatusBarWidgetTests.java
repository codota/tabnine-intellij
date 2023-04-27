package com.tabnine.healthCheck;

import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.testUtils.HealthCheckTestUtils;
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnineCommon.general.StaticConfig;
import com.tabnineCommon.statusBar.TabnineStatusBarWidget;
import org.junit.Test;

public class StatusBarWidgetTests extends MockedBinaryCompletionTestCase {

  @Test
  public void should_get_connection_healthy_icon_when_connection_healthy() {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    HealthCheckTestUtils.notifyHealthStatus(CloudConnectionHealthStatus.Ok);

    assertEquals(widget.getIcon(), StaticConfig.ICON_AND_NAME_STARTER);
  }

  @Test
  public void should_get_connection_unhealthy_icon_when_connection_unhealthy() {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    HealthCheckTestUtils.notifyHealthStatus(CloudConnectionHealthStatus.Failed);

    assertEquals(widget.getIcon(), StaticConfig.ICON_AND_NAME_CONNECTION_LOST_STARTER);
  }
}
