package com.tabnine;

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginManagerCore;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.lifecycle.BinaryNotificationsLifecycle;
import com.tabnine.lifecycle.BinaryPromotionStatusBarLifecycle;
import com.tabnine.lifecycle.TabNineDisablePluginListener;
import com.tabnine.lifecycle.TabnineUpdater;
import com.tabnine.logging.LogInitializerKt;

import static com.tabnine.general.DependencyContainer.*;

public class Initializer implements ApplicationInitializedListener {
    private TabNineDisablePluginListener listener;
    private BinaryNotificationsLifecycle binaryNotificationsLifecycle;
    private BinaryPromotionStatusBarLifecycle binaryPromotionStatusBarLifecycle;

    @Override
    public void componentsInitialized() {
        LogInitializerKt.init();
        listener = singletonOfTabNineDisablePluginListener();
        binaryNotificationsLifecycle = instanceOfBinaryNotifications();
        binaryPromotionStatusBarLifecycle = instanceOfBinaryPromotionStatusBar();
        PluginManagerCore.addDisablePluginListener(listener::onDisable);
        PluginInstaller.addStateListener(instanceOfTabNinePluginStateListener());
        binaryNotificationsLifecycle.poll();
        binaryPromotionStatusBarLifecycle.poll();
        CapabilitiesService.getInstance().init();
        TabnineUpdater.pollUpdates();
    }
}
