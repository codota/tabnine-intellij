package com.tabnine;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.ApplicationLoadListener;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.tabnine.lifecycle.TabNineDisablePluginListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.tabnine.general.DependencyContainer.instanceOfTabNinePluginStateListener;
import static com.tabnine.general.DependencyContainer.singletonOfTabNineDisablePluginListener;

public class Initializer implements ApplicationLoadListener, AppLifecycleListener {
    private final TabNineDisablePluginListener listener = singletonOfTabNineDisablePluginListener();
    @Override
    public void beforeApplicationLoaded(@NotNull Application application, @NotNull String configPath) {
        final MessageBusConnection connection = application.getMessageBus().connect();

        connection.subscribe(AppLifecycleListener.TOPIC, this);
    }

    @Override
    public void appStarting(@Nullable Project projectFromCommandLine) {
        PluginManagerCore.addDisablePluginListener(listener::onDisable);
        PluginInstaller.addStateListener(instanceOfTabNinePluginStateListener());
    }

}