package com.tabnine.integration;

import static com.tabnine.testUtils.StateChangedTopicUtils.getMockedStateResponse;
import static com.tabnine.testUtils.StateChangedTopicUtils.notifyStateChangedTopic;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.notifications.ConnectionLostNotificationHandler;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ConnectionLostNotificationTests extends MockedBinaryCompletionTestCase {

  @Test
  public void should_show_connection_lost_notification_when_connection_unhealthy() {
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect()
        .subscribe(
            Notifications.TOPIC,
            new Notifications() {
              @Override
              public void notify(@NotNull Notification notification) {
                assertTrue(notification.getContent().contains("Tabnine lost internet connection"));
              }
            });

    new ConnectionLostNotificationHandler().handleConnectionLostEvent();
    notifyStateChangedTopic(getMockedStateResponse(false));
  }

  @Test
  public void should_show_connection_lost_notification_once_in_interval_time() {
    AtomicInteger numOfNotificationAppearance = new AtomicInteger(0);
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect()
        .subscribe(
            Notifications.TOPIC,
            new Notifications() {
              @Override
              public void notify(@NotNull Notification notification) {
                if (notification.getContent().contains("Tabnine lost internet connection")) {
                  numOfNotificationAppearance.set(numOfNotificationAppearance.get() + 1);
                }
              }
            });

    new ConnectionLostNotificationHandler().handleConnectionLostEvent();
    for (int i = 0; i < 10; i++) {
      notifyStateChangedTopic(getMockedStateResponse(false));
    }
    assertEquals(1, numOfNotificationAppearance.get());
  }

  @Test
  public void should_register_once_to_state_response_changed_on_multiple_calls() {
    AtomicInteger numOfNotificationAppearance = new AtomicInteger(0);
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect()
        .subscribe(
            Notifications.TOPIC,
            new Notifications() {
              @Override
              public void notify(@NotNull Notification notification) {
                if (notification.getContent().contains("Tabnine lost internet connection")) {
                  numOfNotificationAppearance.set(numOfNotificationAppearance.get() + 1);
                }
              }
            });

    ConnectionLostNotificationHandler connectionLostNotificationHandler =
        new ConnectionLostNotificationHandler();
    connectionLostNotificationHandler.handleConnectionLostEvent();
    connectionLostNotificationHandler.handleConnectionLostEvent();
    for (int i = 0; i < 10; i++) {
      notifyStateChangedTopic(getMockedStateResponse(false));
    }
    assertEquals(1, numOfNotificationAppearance.get());
  }
}
