package com.tabnine.capabilities;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.capabilities.CapabilitiesRequest;
import com.tabnine.binary.requests.capabilities.CapabilitiesResponse;
import com.tabnine.general.DependencyContainer;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CapabilitiesService {

  private Thread refreshLoop = null;

  private final BinaryRequestFacade binaryRequestFacade =
      DependencyContainer.instanceOfBinaryRequestFacade();
  private final Set<Capability> enabledCapabilities = new HashSet<>();

  public static CapabilitiesService getInstance() {
    return ServiceManager.getService(CapabilitiesService.class);
  }

  public void init() {
    scheduleFetchCapabilitiesTask();
  }

  public boolean isCapabilityEnabled(Capability capability) {
    synchronized (enabledCapabilities) {
      return enabledCapabilities.contains(capability);
    }
  }

  private synchronized void scheduleFetchCapabilitiesTask() {
    if (refreshLoop == null) {
      refreshLoop = new Thread(() -> this.fetchCapabilitiesLoop());
      refreshLoop.setDaemon(true);
      refreshLoop.start();
    }
  }

  public static final int INITIAL_DELAY_MS = 2000;
  public static final int LOOP_INTERVAL_MS = 1000;

  public static final int REFRESH_EVERY_MS = 10 * 60 * 1000; // 10 minutes

  private void fetchCapabilitiesLoop() {
    Optional<Long> lastRefresh = Optional.empty();
    Optional<Long> lastPid = Optional.empty();

    try {
      Thread.sleep(INITIAL_DELAY_MS);

      while (true) {
        try {
          Long pid = binaryRequestFacade.pid();
          if (!lastRefresh.isPresent()
              || lastRefresh.get() - System.currentTimeMillis() >= REFRESH_EVERY_MS
              || !lastPid.isPresent()
              || lastPid.get() == null
              || !lastPid.get().equals(pid)) {
            fetchCapabilities();

            lastRefresh = Optional.of(System.currentTimeMillis());
            lastPid = Optional.of(pid);
          }
        } catch (Throwable t) {
          Logger.getInstance(getClass()).warn("Unexpected error. Capabilities refresh failed", t);
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
    if (capabilitiesResponse != null && capabilitiesResponse.getEnabledFeatures() != null) {
      setCapabilities(capabilitiesResponse);
    } else {
      scheduleFetchCapabilitiesTask();
    }
  }

  private void setCapabilities(CapabilitiesResponse capabilitiesResponse) {
    synchronized (enabledCapabilities) {
      enabledCapabilities.clear();
      capabilitiesResponse.getEnabledFeatures().stream()
          .filter(Objects::nonNull)
          .forEach(enabledCapabilities::add);
    }
  }
}
