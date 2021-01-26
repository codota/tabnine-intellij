package com.tabnine.lifecycle;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.config.StateRequest;
import com.tabnine.binary.requests.config.StateResponse;
import com.tabnine.general.DependencyContainer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BinaryStateService implements Disposable {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final BinaryRequestFacade binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade();
    private final MessageBus messageBus;
    private StateResponse lastStateResponse;

    public BinaryStateService() {
        this.messageBus = ApplicationManager.getApplication().getMessageBus();;
        scheduler.scheduleAtFixedRate(this::updateState, 0, 1, TimeUnit.SECONDS);
    }

    public StateResponse getLastStateResponse() {
        return this.lastStateResponse;
    }

    private void updateState() {
        final StateResponse stateResponse = this.binaryRequestFacade.executeRequest(new StateRequest());
        if (stateResponse != null) {
            if (!stateResponse.equals(this.lastStateResponse)) {
                this.messageBus.syncPublisher(BinaryStateChangeNotifier.STATE_CHANGED_TOPIC)
                        .stateChanged(stateResponse);
            }
            this.lastStateResponse = stateResponse;
        }
    }

    @Override
    public void dispose() {
        scheduler.shutdownNow();
    }
}
