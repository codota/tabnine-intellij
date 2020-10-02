package com.tabnine.binary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.StaticConfig;
import com.tabnine.contracts.AutocompleteRequest;
import com.tabnine.contracts.AutocompleteResponse;
import com.tabnine.exceptions.TabNineDeadException;
import com.tabnine.exceptions.TabNineInvalidResponseException;
import com.tabnine.exceptions.TooManyConsecutiveRestartsException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tabnine.StaticConfig.*;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;

public class TabNineGateway {
    private final Gson gson = new GsonBuilder().create();
    private final AtomicBoolean isRestarting = new AtomicBoolean(false);
    private int consecutiveRestarts = 0;
    private int illegalResponsesGiven = 0;
    private Future<?> binaryInit = null;

    public void init() {
        if (isRestarting.compareAndSet(false, true)) {
            startBinary(TabNineProcessFacade::create);
        }
    }

    /**
     * Restarts the binary's process. This is thread-safe. Should be called on start, and could be called to restart
     * the binary.
     */
    public void restart() {
        // In case of a restart already underway, no need to restart again. Just wait for it...
        if (!isStarting() && isRestarting.compareAndSet(false, true)) {
            if (++this.consecutiveRestarts > StaticConfig.CONSECUTIVE_RESTART_THRESHOLD) {
                Logger.getInstance(getClass()).error("Tabnine is not able to function properly", new TooManyConsecutiveRestartsException());
            }

            startBinary(TabNineProcessFacade::restart);
        }
    }

    private void startBinary(SideEffectExecutor onStartBinaryAttempt) {
        binaryInit = AppExecutorUtil.getAppExecutorService().submit(() -> {
            for (int attempt = 0; shouldTryStartingBinary(attempt); attempt++) {
                try {
                    onStartBinaryAttempt.execute();
                    isRestarting.set(false);
                    break;
                } catch (IOException | NoValidBinaryToRunException e) {
                    Logger.getInstance(getClass()).warn("Error restarting TabNine. Will try again.", e);

                    try {
                        sleepUponFailure(attempt);
                    } catch (InterruptedException e2) {
                        Logger.getInstance(getClass()).error("TabNine was interrupted between restart attempts.", e);

                        break;
                    }
                }
            }
        });
    }

    /**
     * Request a prediction from TabNine's binary.
     *
     * @param request
     * @return an AutocompleteResponse
     * @throws TabNineDeadException if process's dead.
     * @throws TabNineDeadException if process's BufferedReader has reached its end (also mean dead...).
     * @throws TabNineDeadException if there was an IOException communicating to the process.
     * @throws TabNineDeadException if the result from the process was invalid multiple times.
     */
    public AutocompleteResponse request(AutocompleteRequest request) throws TabNineDeadException {
        if (isStarting()) {
            Logger.getInstance(getClass()).info("Can't get completions because TabNine process is not started yet.");
            return null;
        }

        try {
            if (TabNineProcessFacade.isDead()) {
                throw new TabNineDeadException("Binary is dead");
            }

            int correlationId = TabNineProcessFacade.getAndIncrementCorrelationId();

            TabNineProcessFacade.writeRequest(serializeRequest(request, correlationId));

            return readResult(request, correlationId);
        } catch (IOException e) {
            Logger.getInstance(getClass()).warn("Exception communicating with the binary!", e);

            throw new TabNineDeadException(e);
        } catch (TabNineInvalidResponseException e) {
            Logger.getInstance(getClass()).warn("", e);

            return null;
        }
    }

    @NotNull
    private AutocompleteResponse readResult(AutocompleteRequest request, int correlationId) throws IOException, TabNineDeadException, TabNineInvalidResponseException {
        while (true) {
            String rawResponse = TabNineProcessFacade.readLine();

            try {
                AutocompleteResponse response = gson.fromJson(rawResponse, request.response());

                if (response.correlation_id == null) {
                    Logger.getInstance(getClass()).warn("Binary is not returning correlation id (grumpy old version?)");
                }

                if (response.correlation_id == null || response.correlation_id == correlationId) {
                    if (!request.validate(response)) {
                        throw new TabNineInvalidResponseException();
                    }

                    onValidResult();

                    return response;
                } else if (response.correlation_id > correlationId) {
                    // This should not happen, as the requests are sequential, but if it occurs, we might as well restart the binary.
                    // If this happens to users, a better readResponse that can lookup the past should be implemented.
                    throw new TabNineDeadException(
                            format("Response from the future received (recieved %d, currently at %d)",
                                    response.correlation_id, correlationId)
                    );
                }
            } catch (JsonSyntaxException | TabNineInvalidResponseException e) {
                Logger.getInstance(getClass()).warn(format("Binary returned illegal response: %s", rawResponse), e);

                if (++illegalResponsesGiven > ILLEGAL_RESPONSE_THRESHOLD) {
                    illegalResponsesGiven = 0;
                    throw new TabNineDeadException("Too many illegal responses given");
                } else {
                    throw new TabNineInvalidResponseException(e);
                }
            }
        }
    }

    private boolean isStarting() {
        return this.binaryInit == null || !this.binaryInit.isDone();
    }

    @NotNull
    private String serializeRequest(AutocompleteRequest request, int correlationId) {
        Map<String, Object> jsonObject = new HashMap<>();

        jsonObject.put("version", StaticConfig.BINARY_PROTOCOL_VERSION);
        jsonObject.put("request", singletonMap(request.name(), request.withCorrelationId(correlationId)));

        return gson.toJson(jsonObject) + "\n";
    }

    /**
     * Reset restart counter once a valid response is sent. This way, we only stop retrying to restart tabnine if there
     * is a chain of failures.
     */
    private void onValidResult() {
        this.consecutiveRestarts = 0;
        this.illegalResponsesGiven = 0;
    }
}
