package com.tabnine;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.ApplicationLoadListener;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.tabnine.lifecycle.BinaryNotificationsLifecycle;
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnine.lifecycle.TabNineDisablePluginListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.tabnine.general.DependencyContainer.*;

public class Initializer implements ApplicationLoadListener, AppLifecycleListener {
    private TabNineDisablePluginListener listener;
    private BinaryNotificationsLifecycle binaryNotificationsLifecycle;
    private BinaryPromotionStatusBarLifecycle binaryPromotionStatusBarLifecycle;

    @Override
    public void beforeApplicationLoaded(@NotNull Application application, @NotNull String configPath) {
        final MessageBusConnection connection = application.getMessageBus().connect();

        connection.subscribe(AppLifecycleListener.TOPIC, this);
    }

    @Override
    public void appStarting(@Nullable Project projectFromCommandLine) {
        listener = singletonOfTabNineDisablePluginListener();
        binaryNotificationsLifecycle = instanceOfBinaryNotifications();
        binaryPromotionStatusBarLifecycle = instanceOfBinaryPromotionStatusBar();
        PluginManagerCore.addDisablePluginListener(listener::onDisable);
        PluginInstaller.addStateListener(instanceOfTabNinePluginStateListener());
        binaryNotificationsLifecycle.poll();
        binaryPromotionStatusBarLifecycle.poll();
    }
}