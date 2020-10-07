package com.tabnine;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.ApplicationLoadListener;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabNineInitializer implements ApplicationLoadListener, AppLifecycleListener {
    @Override
    public void beforeApplicationLoaded(@NotNull Application application, @NotNull String configPath) {
        final MessageBusConnection connection = application.getMessageBus().connect();
        connection.subscribe(AppLifecycleListener.TOPIC, this);
    }

    @Override
    public void appStarting(@Nullable Project projectFromCommandLine) {
        PluginInstaller.addStateListener(DependencyContainer.instanceOfTabNinePluginStateListener());
    }
}