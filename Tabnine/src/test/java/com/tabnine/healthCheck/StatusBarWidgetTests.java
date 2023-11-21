package com.tabnine.healthCheck;

import com.google.common.collect.Sets;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.statusBar.TabnineStatusBarWidget;
import com.tabnine.testUtils.HealthCheckTestUtils;
import com.tabnineCommon.binary.requests.capabilities.ExperimentSource;
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnineCommon.capabilities.Capabilities;
import com.tabnineCommon.capabilities.Capability;
import com.tabnineCommon.general.ServiceLevel;
import com.tabnineCommon.general.StaticConfig;
import com.tabnineCommon.lifecycle.CapabilitiesStateSingleton;
import java.util.HashSet;
import org.junit.Test;

public class StatusBarWidgetTests extends MockedBinaryCompletionTestCase {

  @Test
  public void should_get_connection_healthy_icon_when_connection_healthy() {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    HealthCheckTestUtils.notifyStateForWidget(
        ServiceLevel.FREE, true, CloudConnectionHealthStatus.Ok);
    CapabilitiesStateSingleton.getInstance()
        .set(new Capabilities(new HashSet<>(), ExperimentSource.API));

    assertEquals(StaticConfig.getIconAndNameStarter(), widget.getIcon());
  }

  @Test
  public void should_get_connection_unhealthy_icon_when_connection_unhealthy() {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    HealthCheckTestUtils.notifyStateForWidget(
        ServiceLevel.FREE, true, CloudConnectionHealthStatus.Failed);
    CapabilitiesStateSingleton.getInstance()
        .set(new Capabilities(new HashSet<>(), ExperimentSource.API));

    assertEquals(StaticConfig.getIconAndNameConnectionLostStarter(), widget.getIcon());
  }

  @Test
  public void
      given_not_ready_source_then_ready_source_when_get_icon_should_return_no_suffix_then_suffix() {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    HealthCheckTestUtils.notifyStateForWidget(
        ServiceLevel.PRO, true, CloudConnectionHealthStatus.Ok);
    CapabilitiesStateSingleton.getInstance()
        .set(new Capabilities(new HashSet<>(), ExperimentSource.Unknown));

    assertEquals(StaticConfig.getIconAndName(), widget.getIcon());

    CapabilitiesStateSingleton.getInstance()
        .set(
            new Capabilities(
                Sets.immutableEnumSet(Capability.FORCE_REGISTRATION), ExperimentSource.API));

    assertEquals(StaticConfig.getIconAndNamePro(), widget.getIcon());
  }
}
