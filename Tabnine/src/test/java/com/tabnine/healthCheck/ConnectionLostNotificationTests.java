package com.tabnine.healthCheck;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.tabnine.MockedBinaryCompletionTestCase;
import com.tabnine.testUtils.HealthCheckTestUtils;
import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus;
import com.tabnineCommon.notifications.ConnectionLostNotificationHandler;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ConnectionLostNotificationTests extends MockedBinaryCompletionTestCase {
  private static final String LOST_CONNECTION_MESSAGE = "Tabnine lost internet connection";

  @Test
  public void should_show_connection_lost_notification_when_connection_unhealthy() {
    ServiceManager.getMessageBus()
        .connect()
        .subscribe(
            Notifications.TOPIC,
            new Notifications() {
              @Override
              public void notify(@NotNull Notification notification) {
                assertTrue(notification.getContent().contains(LOST_CONNECTION_MESSAGE));
              }
            });

    new ConnectionLostNotificationHandler("OH NO").startConnectionLostListener();
    HealthCheckTestUtils.notifyHealthStatus(CloudConnectionHealthStatus.Failed);
  }

  @Test
  public void should_show_connection_lost_notification_once_in_interval_time() {
    AtomicInteger numOfNotificationAppearance = new AtomicInteger(0);
    ServiceManager.getMessageBus()
        .connect()
        .subscribe(
            Notifications.TOPIC,
            new Notifications() {
              @Override
              public void notify(@NotNull Notification notification) {
                if (notification.getContent().contains(LOST_CONNECTION_MESSAGE)) {
                  numOfNotificationAppearance.incrementAndGet();
                }
              }
            });

    new ConnectionLostNotificationHandler("OH NO").startConnectionLostListener();
    HealthCheckTestUtils.notifyHealthStatus(CloudConnectionHealthStatus.Failed, 10);
    assertEquals(1, numOfNotificationAppearance.get());
  }

  @Test
  public void should_register_once_to_state_response_changed_on_multiple_calls() {
    AtomicInteger numOfNotificationAppearance = new AtomicInteger(0);
    ServiceManager.getMessageBus()
        .connect()
        .subscribe(
            Notifications.TOPIC,
            new Notifications() {
              @Override
              public void notify(@NotNull Notification notification) {
                if (notification.getContent().contains(LOST_CONNECTION_MESSAGE)) {
                  numOfNotificationAppearance.incrementAndGet();
                }
              }
            });

    ConnectionLostNotificationHandler connectionLostNotificationHandler =
        new ConnectionLostNotificationHandler("OH NO");
    connectionLostNotificationHandler.startConnectionLostListener();
    connectionLostNotificationHandler.startConnectionLostListener();
    HealthCheckTestUtils.notifyHealthStatus(CloudConnectionHealthStatus.Failed, 10);
    assertEquals(1, numOfNotificationAppearance.get());
  }
}
