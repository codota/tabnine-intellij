package com.tabnineCommon.capabilities;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.messages.MessageBus;
import com.tabnineCommon.binary.BinaryRequestFacade;
import com.tabnineCommon.binary.requests.capabilities.CapabilitiesRequest;
import com.tabnineCommon.binary.requests.capabilities.CapabilitiesResponse;
import com.tabnineCommon.config.Config;
import com.tabnineCommon.general.DependencyContainer;
import com.tabnineCommon.lifecycle.BinaryCapabilitiesChangeNotifier;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class CapabilitiesService {

  public static final int LOOP_INTERVAL_MS = 1000;

  public static final int REFRESH_EVERY_MS = 10 * 1000; // 10 secs

  private Thread refreshLoop = null;
  private final MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();

  private final BinaryRequestFacade binaryRequestFacade =
      DependencyContainer.instanceOfBinaryRequestFacade();
  private final Set<Capability> enabledCapabilities = new HashSet<>();
  private final AtomicBoolean isRemoteBasedSource = new AtomicBoolean(false);

  public static CapabilitiesService getInstance() {
    return ServiceManager.getService(CapabilitiesService.class);
  }

  public void init() {
    scheduleFetchCapabilitiesTask();
  }

  public boolean isReady() {
    return this.isRemoteBasedSource.get();
  }

  public boolean isCapabilityEnabled(Capability capability) {
    if (Config.IS_SELF_HOSTED) {
      return true;
    }
    synchronized (enabledCapabilities) {
      return enabledCapabilities.contains(capability);
    }
  }

  private synchronized void scheduleFetchCapabilitiesTask() {
    if (refreshLoop == null) {
      refreshLoop = new Thread(this::fetchCapabilitiesLoop);
      refreshLoop.setDaemon(true);
      refreshLoop.start();
    }
  }

  private void fetchCapabilitiesLoop() {
    Optional<Long> lastRefresh = Optional.empty();
    Optional<Long> lastPid = Optional.empty();

    try {
      while (true) {
        try {
          Long pid = binaryRequestFacade.pid();
          boolean expiredSinceLastRefresh =
              !lastRefresh.isPresent()
                  || System.currentTimeMillis() - lastRefresh.get() >= REFRESH_EVERY_MS;

          boolean pidChanged =
              !lastPid.isPresent() || lastPid.get() == null || !lastPid.get().equals(pid);

          if (expiredSinceLastRefresh || pidChanged) {
            fetchCapabilities();

            lastRefresh = Optional.of(System.currentTimeMillis());
            lastPid = Optional.of(pid);
          }
          if (!enabledCapabilities.isEmpty()) {
            this.messageBus
                .syncPublisher(BinaryCapabilitiesChangeNotifier.CAPABILITIES_CHANGE_NOTIFIER_TOPIC)
                .notifyFetched();
          }
        } catch (Throwable t) {
          Logger.getInstance(getClass()).debug("Unexpected error. Capabilities refresh failed", t);
        }

        Thread.sleep(LOOP_INTERVAL_MS);
      }
    } catch (Throwable t) {
      Logger.getInstance(getClass()).warn("Unexpected error. Capabilities refresh loop exiting", t);
    }
  }

  private void fetchCapabilities() {
    final CapabilitiesResponse capabilitiesResponse =
        binaryRequestFacade.executeRequest(new CapabilitiesRequest());

    if (capabilitiesResponse == null) {
      return;
    }

    if (capabilitiesResponse.getEnabledFeatures() != null) {
      setCapabilities(capabilitiesResponse);
    }

    if (capabilitiesResponse.getExperimentSource() == null
        || capabilitiesResponse.getExperimentSource().isRemoteBasedSource()) {
      isRemoteBasedSource.set(true);
    }
  }

  private void setCapabilities(CapabilitiesResponse capabilitiesResponse) {
    synchronized (enabledCapabilities) {
      Set<Capability> newCapabilities = new HashSet<>();

      capabilitiesResponse.getEnabledFeatures().stream()
          .filter(Objects::nonNull)
          .forEach(newCapabilities::add);

      if (!newCapabilities.equals(enabledCapabilities)) {
        enabledCapabilities.clear();
        enabledCapabilities.addAll(newCapabilities);
        CapabilityNotifier.Companion.publish(enabledCapabilities);
      }
    }
  }
}
