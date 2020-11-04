package com.tabnine.binary;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.exceptions.BinaryCannotRecoverException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.exceptions.TooManyConsecutiveRestartsException;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tabnine.general.StaticConfig.*;

public class BinaryRequestFacade {
    private final Queue<Future<?>> activeRequests = new ConcurrentLinkedQueue<>();
    private final AtomicInteger consecutiveRestarts = new AtomicInteger(0);
    private final AtomicInteger consecutiveTimeouts = new AtomicInteger(0);
    private final TabNineGateway process;

    public BinaryRequestFacade(TabNineGateway process) {
        this.process = process;
    }

    public <R extends BinaryResponse> R executeRequest(BinaryRequest<R> req) throws BinaryCannotRecoverException {
        return executeRequest(req, COMPLETION_TIME_THRESHOLD);
    }

    @Nullable
    public <R extends BinaryResponse> R executeRequest(BinaryRequest<R> req, int timeoutMillis) throws BinaryCannotRecoverException {
        if (process.isStarting()) {
            Logger.getInstance(getClass()).info("Can't get completions because TabNine process is not started yet.");

            return null;
        }
        try {
            Future<R> request = AppExecutorUtil.getAppExecutorService().submit(() -> {
                try {
                    R result = process.request(req);

                    consecutiveRestarts.set(0);

                    return result;
                } catch (TabNineDeadException e) {
                    Logger.getInstance(getClass()).warn("TabNine is in invalid state, it is being restarted.", e);

                    if (consecutiveRestarts.incrementAndGet() > CONSECUTIVE_RESTART_THRESHOLD) {
                        Logger.getInstance(getClass()).error("Tabnine is not able to function properly. Contact support@tabnine.com", new TooManyConsecutiveRestartsException());
                        // NOTICE: In the production version of IntelliJ, logging an error kills the plugin. So this is similar to exit(1);
                        throw new BinaryCannotRecoverException("Tabnine is not able to function properly. Contact support@tabnine.com");
                    } else {
                        restartBinary();
                    }

                    return null;
                } finally {
                    activeRequests.removeIf(Future::isDone);
                }
            });

            activeRequests.add(request);

            R result = request.get(timeoutMillis, TimeUnit.MILLISECONDS);

            consecutiveTimeouts.set(0);

            return result;
        } catch (TimeoutException e) {
            Logger.getInstance(getClass()).info("TabNine's response timed out.");

            if (consecutiveTimeouts.incrementAndGet() >= CONSECUTIVE_TIMEOUTS_THRESHOLD) {
                Logger.getInstance(getClass()).warn("Requests to TabNine's binary are consistently taking too long. Restarting the binary.", e);
                consecutiveTimeouts.set(0);
                restartBinary();
            }
        } catch (BinaryCannotRecoverException e) {
            throw e;
        } catch (CancellationException e) {
            // This is ok. Nothing needs to be done.
        } catch (Exception e) {
            Logger.getInstance(getClass()).error("TabNine's threw an unknown error.", e);
        }

        return null;
    }

    private void restartBinary() {
        this.activeRequests.forEach(f -> f.cancel(true));
        this.activeRequests.clear();
        this.process.restart();
    }
}
