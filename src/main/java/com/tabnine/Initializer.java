package com.tabnine;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.ApplicationLoadListener;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.tabnine.config.Config;
import com.tabnine.general.SentryAppender;
import com.tabnine.lifecycle.BinaryNotificationsLifecycle;
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnine.lifecycle.TabNineDisablePluginListener;
import org.apache.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.sentry.Sentry;

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
        initLogger();
        listener = singletonOfTabNineDisablePluginListener();
        binaryNotificationsLifecycle = instanceOfBinaryNotifications();
        binaryPromotionStatusBarLifecycle = instanceOfBinaryPromotionStatusBar();
        PluginManagerCore.addDisablePluginListener(listener::onDisable);
        PluginInstaller.addStateListener(instanceOfTabNinePluginStateListener());
        binaryNotificationsLifecycle.poll();
        binaryPromotionStatusBarLifecycle.poll();
    }

    private void initLogger() {
        Sentry.init();
        Sentry.configureScope(scope -> {
            scope.setTag("ide", ApplicationInfo.getInstance().getVersionName());
            scope.setTag("ideVersion", ApplicationInfo.getInstance().getFullVersion());
            scope.setTag("os", System.getProperty("os.name"));
            scope.setTag("channel", Config.CHANNEL);
        });
        org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
        rootLogger.addAppender(new SentryAppender());
    }
}