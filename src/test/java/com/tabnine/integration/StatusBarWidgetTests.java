package com.tabnine.integration;

import static com.tabnine.testUtils.StateChangedTopicUtils.getMockedStateResponse;
import static com.tabnine.testUtils.StateChangedTopicUtils.notifyStateChangedTopic;

import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.general.StaticConfig;
import com.tabnine.statusBar.TabnineStatusBarWidget;
import org.junit.Test;

public class StatusBarWidgetTests extends MockedBinaryCompletionTestCase {

  @Test
  public void should_get_connection_healthy_icon_when_connection_healthy() {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    notifyStateChangedTopic(getMockedStateResponse(true));

    assertEquals(widget.getIcon(), StaticConfig.ICON_AND_NAME_STARTER);
  }

  @Test
  public void should_get_connection_unhealthy_icon_when_connection_unhealthy() {
    TabnineStatusBarWidget widget = new TabnineStatusBarWidget(myFixture.getProject());
    notifyStateChangedTopic(getMockedStateResponse(false));

    assertEquals(widget.getIcon(), StaticConfig.ICON_AND_NAME_CONNECTION_LOST_STARTER);
  }
}
