package com.tabnine.binary;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.exceptions.TabNineInvalidResponseException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tabnine.general.StaticConfig.*;

public class TabNineGateway {
    private final AtomicBoolean isRestarting = new AtomicBoolean(false);
    private int illegalResponsesGiven = 0;
    private Future<?> binaryInit = null;

    public boolean isStarting() {
        return this.binaryInit == null || !this.binaryInit.isDone() || this.isRestarting.get();
    }

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
    @Nullable
    public synchronized <R extends BinaryResponse> R request(BinaryRequest<R> request) throws TabNineDeadException, TabNineInvalidResponseException {
        if (TabNineProcessFacade.isDead()) {
            throw new TabNineDeadException("Binary is dead");
        }

        try {
            TabNineProcessFacade.writeRequest(wrapWithBinaryRequest(request.serialize()));

            return readResult(request);
        } catch (IOException e) {
            Logger.getInstance(getClass()).warn("Exception communicating with the binary!", e);

            throw new TabNineDeadException(e);
        }
    }

    @Nullable
    private <R extends BinaryResponse> R readResult(BinaryRequest<R> request) throws IOException, TabNineDeadException, TabNineInvalidResponseException {
        try {
            R response = TabNineProcessFacade.readLine(request.response());

            if (!request.validate(response)) {
                throw new TabNineInvalidResponseException();
            }

            this.illegalResponsesGiven = 0;

            return response;
        } catch (TabNineInvalidResponseException exception) {
            if (request.shouldBeAllowed(exception)) {
                return null;
            }

            if (++illegalResponsesGiven > ILLEGAL_RESPONSE_THRESHOLD) {
                illegalResponsesGiven = 0;
                throw new TabNineDeadException("Too many illegal responses given");
            } else {
                throw exception;
            }
        }
    }
}
