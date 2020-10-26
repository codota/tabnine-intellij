package com.tabnine.binary;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
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

    public <R extends BinaryResponse> R executeRequest(BinaryRequest<R> req) {
        return executeRequest(req, COMPLETION_TIME_THRESHOLD);
    }

    @Nullable
    public <R extends BinaryResponse> R executeRequest(BinaryRequest<R> req, int timeoutMillis) {
        if (process.isStarting()) {
            Logger.getInstance(getClass()).info("Can't get completions because TabNine process is not started yet.");

            return null;
        }
        try {
            Future<R> request = AppExecutorUtil.getAppExecutorService().submit(() -> {
                try {
                    return process.request(req);
                } catch (TabNineDeadException e) {
                    Logger.getInstance(getClass()).warn("TabNine is in invalid state, it is being restarted.", e);

                    if (consecutiveRestarts.incrementAndGet() > CONSECUTIVE_RESTART_THRESHOLD) {
                        Logger.getInstance(getClass()).error("Tabnine is not able to function properly. Contact support@tabnine.com", new TooManyConsecutiveRestartsException());
                        consecutiveRestarts.set(0);
                    } else {
                        restartBinary();
                    }

                    return null;
                } finally {
                    activeRequests.removeIf(Future::isDone);
                }
            });

            activeRequests.add(request);

            return request.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            Logger.getInstance(getClass()).info("TabNine's response timed out.");

            if (consecutiveTimeouts.incrementAndGet() >= CONSECUTIVE_TIMEOUTS_THRESHOLD) {
                Logger.getInstance(getClass()).warn("Requests to TabNine's binary are consistently taking too long. Restarting the binary.", e);
                consecutiveTimeouts.set(0);
                restartBinary();
            }
        } catch (CancellationException e) {
            // This is ok. Nothing needs to be done.
        } catch (Throwable t) {
            Logger.getInstance(getClass()).error("TabNine's threw an unknown error.", t);
        }

        return null;
    }

    private void restartBinary() {
        this.activeRequests.forEach(f -> f.cancel(true));
        this.activeRequests.clear();
        this.process.restart();
    }
}
