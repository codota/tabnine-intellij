package com.tabnine.capabilities;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.capabilities.CapabilitiesRequest;
import com.tabnine.binary.requests.capabilities.CapabilitiesResponse;
import com.tabnine.general.DependencyContainer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CapabilitiesService {

  private final BinaryRequestFacade binaryRequestFacade =
      DependencyContainer.instanceOfBinaryRequestFacade();
  private final Set<Capability> enabledCapabilities = new HashSet<>();
  private ScheduledFuture<?> fetchCapabilitiesFuture;

  public static CapabilitiesService getInstance() {
    return ServiceManager.getService(CapabilitiesService.class);
  }

  public void init() {
    scheduleFetchCapabilitiesTask();
  }

  public boolean isCapabilityEnabled(Capability capability) {
    if (fetchCapabilitiesFuture == null || !fetchCapabilitiesFuture.isDone()) {
      return false;
    }
    return enabledCapabilities.contains(capability);
  }

  private void scheduleFetchCapabilitiesTask() {
    fetchCapabilitiesFuture =
        AppExecutorUtil.getAppScheduledExecutorService()
            .schedule(this::fetchCapabilities, 2, TimeUnit.SECONDS);
  }

  private void fetchCapabilities() {
    final CapabilitiesResponse capabilitiesResponse =
        binaryRequestFacade.executeRequest(new CapabilitiesRequest());
    if (capabilitiesResponse != null && capabilitiesResponse.getEnabledFeatures() != null) {
      capabilitiesResponse.getEnabledFeatures().stream()
          .filter(Objects::nonNull)
          .forEach(enabledCapabilities::add);
    } else {
      scheduleFetchCapabilitiesTask();
    }
  }
}
