package com.tabnine.binary;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.exceptions.BinaryCannotRecoverException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.exceptions.TabNineInvalidResponseException;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tabnine.general.StaticConfig.*;

public class BinaryRequestFacade {
    private final Queue<Future<?>> activeRequests = new ConcurrentLinkedQueue<>();
    private final AtomicInteger consecutiveRestarts = new AtomicInteger(0);
    private final AtomicInteger consecutiveTimeouts = new AtomicInteger(0);
    private final TabNineGateway tabNineGateway;

    public BinaryRequestFacade(TabNineGateway tabNineGateway) {
        this.tabNineGateway = tabNineGateway;
    }

    public <R extends BinaryResponse> R executeRequest(BinaryRequest<R> req) {
        return executeRequest(req, COMPLETION_TIME_THRESHOLD);
    }

    @Nullable
    public <R extends BinaryResponse> R executeRequest(BinaryRequest<R> req, int timeoutMillis) {
        if (tabNineGateway.isStarting()) {
            Logger.getInstance(getClass()).info("Can't get completions because TabNine process is not started yet.");

            return null;
        }

        try {
            Future<R> requestFuture = AppExecutorUtil.getAppExecutorService().submit(() -> executeBoundlessRequest(req));

            activeRequests.add(requestFuture);

            R result = requestFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);

            consecutiveTimeouts.set(0);

            return result;
        } catch (TimeoutException e) {
            Logger.getInstance(getClass()).info("TabNine's response timed out.");

            if (consecutiveTimeouts.incrementAndGet() >= CONSECUTIVE_TIMEOUTS_THRESHOLD) {
                Logger.getInstance(getClass()).warn("Requests to TabNine's binary are consistently taking too long. Restarting the binary.", e);
                consecutiveTimeouts.set(0);
                restartBinary();
            }
        } catch (ExecutionException e) {
            if(e.getCause().getCause() instanceof BinaryCannotRecoverException) {
                throw (BinaryCannotRecoverException) e.getCause().getCause();
            }

            Logger.getInstance(getClass()).warn("TabNine's threw an unknown error during request.", e);
        } catch (CancellationException | InterruptedException e) {
            // This is ok. Nothing needs to be done.
        } catch (Exception e) {
            Logger.getInstance(getClass()).warn("TabNine's threw an unknown error.", e);
        }

        return null;
    }

    @Nullable
    private <R extends BinaryResponse> R executeBoundlessRequest(BinaryRequest<R> req) {
        try {
            R result = tabNineGateway.request(req);

            consecutiveRestarts.set(0);

            return result;
        } catch (TabNineDeadException e) {
            Logger.getInstance(getClass()).warn("TabNine is in invalid state, it is being restarted.", e);

            if (consecutiveRestarts.incrementAndGet() > CONSECUTIVE_RESTART_THRESHOLD) {
                // NOTICE: In the production version of IntelliJ, logging an error kills the plugin. So this is similar to exit(1);
                Logger.getInstance(getClass()).error("Tabnine is not able to function properly. Contact support@tabnine.com", new BinaryCannotRecoverException());
            } else {
                restartBinary();
            }

            return null;
        } catch (TabNineInvalidResponseException e) {
            Logger.getInstance(getClass()).warn(e);

            return null;
        } finally {
            activeRequests.removeIf(Future::isDone);
        }
    }

    private void restartBinary() {
        this.activeRequests.forEach(f -> f.cancel(true));
        this.activeRequests.clear();
        this.tabNineGateway.restart();
    }
}
