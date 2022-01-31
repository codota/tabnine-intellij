package com.tabnine;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.ApplicationLoadListener;
import com.intellij.ide.plugins.DisabledPluginsState;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.openapi.application.Application;
import com.intellij.util.messages.MessageBusConnection;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.lifecycle.BinaryNotificationsLifecycle;
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnine.lifecycle.TabNineDisablePluginListener;
import com.tabnine.lifecycle.TabnineUpdater;
import com.tabnine.logging.LogInitializerKt;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;

import static com.tabnine.general.DependencyContainer.*;

public class Initializer implements ApplicationLoadListener, AppLifecycleListener {
    private TabNineDisablePluginListener listener;
    private BinaryNotificationsLifecycle binaryNotificationsLifecycle;
    private BinaryPromotionStatusBarLifecycle binaryPromotionStatusBarLifecycle;

    @Override
    public void beforeApplicationLoaded(@NotNull Application application, @NotNull Path configPath) {
        final MessageBusConnection connection = application.getMessageBus().connect();

        connection.subscribe(AppLifecycleListener.TOPIC, this);
    }

    @Override
    public void appStarted() {
        LogInitializerKt.init();
        listener = singletonOfTabNineDisablePluginListener();
        binaryNotificationsLifecycle = instanceOfBinaryNotifications();
        binaryPromotionStatusBarLifecycle = instanceOfBinaryPromotionStatusBar();
        DisabledPluginsState.addDisablePluginListener(listener::onDisable);
        PluginInstaller.addStateListener(instanceOfTabNinePluginStateListener());
        binaryNotificationsLifecycle.poll();
        binaryPromotionStatusBarLifecycle.poll();
        CapabilitiesService.getInstance().init();
        TabnineUpdater.pollUpdates();

    }
}
