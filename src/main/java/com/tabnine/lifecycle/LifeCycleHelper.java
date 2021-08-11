package com.tabnine.lifecycle;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;

/**
 * This application service can use as a general parent disposable for plugin components that need
 * to be registered in IntelliJ {@link com.intellij.openapi.util.Disposer}
 */
public class LifeCycleHelper implements Disposable {

  public static LifeCycleHelper getInstance() {
    return ServiceManager.getService(LifeCycleHelper.class);
  }

  @Override
  public void dispose() {
    // Nothing to do, just use it as root disposable parent in application level
  }
}
