package com.tabnine.binary;

import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.exceptions.BinaryCannotRecoverException;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;

import java.io.IOException;
import java.util.concurrent.Future;

import static com.tabnine.general.StaticConfig.*;
import static java.util.Collections.singletonMap;

public class BinaryProcessRequesterProvider {
    private final BinaryRun binaryRun;
    private final BinaryProcessGatewayProvider binaryProcessGatewayProvider;

    private int consecutiveRestarts = 0;
    private int consecutiveTimeouts = 0;
    private BinaryProcessRequester binaryProcessRequester;
    private Future<?> binaryInit;

    private BinaryProcessRequesterProvider(BinaryRun binaryRun, BinaryProcessGatewayProvider binaryProcessGatewayProvider) {
        this.binaryRun = binaryRun;
        this.binaryProcessGatewayProvider = binaryProcessGatewayProvider;
    }

    public static BinaryProcessRequesterProvider create(BinaryRun binaryRun, BinaryProcessGatewayProvider binaryProcessGatewayProvider) {
        BinaryProcessRequesterProvider binaryProcessRequesterProvider = new BinaryProcessRequesterProvider(binaryRun, binaryProcessGatewayProvider);

        binaryProcessRequesterProvider.createNew();

        return binaryProcessRequesterProvider;
    }

    public BinaryProcessRequester get() {
        if(isStarting()) {
            Logger.getInstance(getClass()).info("Can't get completions because Tabnine process is not started yet.");

            return VoidBinaryProcessRequester.instance();
        }

        return binaryProcessRequester;
    }

    public void onSuccessfulRequest() {
        consecutiveTimeouts = 0;
        consecutiveRestarts = 0;
    }

    public void onDead(Throwable e) {
        consecutiveTimeouts = 0;
        Logger.getInstance(getClass()).warn("Tabnine is in invalid state, it is being restarted.", e);

        if (++consecutiveRestarts > CONSECUTIVE_RESTART_THRESHOLD) {
            // NOTICE: In the production version of IntelliJ, logging an error kills the plugin. So this is similar to exit(1);
            Logger.getInstance(getClass()).error("Tabnine is not able to function properly. Contact support@tabnine.com", new BinaryCannotRecoverException());
        } else {
            createNew();
        }
    }

    public void onTimeout() {
        Logger.getInstance(getClass()).info("TabNine's response timed out.");

        if (++consecutiveTimeouts >= CONSECUTIVE_TIMEOUTS_THRESHOLD) {
            Logger.getInstance(getClass()).warn("Requests to TabNine's binary are consistently taking too long. Restarting the binary.");
            createNew();
        }
    }

    private boolean isStarting() {
        return this.binaryInit == null || !this.binaryInit.isDone();
    }

    private void createNew() {
        BinaryProcessGateway binaryProcessGateway = binaryProcessGatewayProvider.generateBinaryProcessGateway();

        initProcess(binaryProcessGateway);

        this.binaryProcessRequester = new BinaryProcessRequesterImpl(new ParsedBinaryIO(new GsonBuilder().create(), binaryProcessGateway));
    }

    private void initProcess(BinaryProcessGateway binaryProcessGateway) {
        binaryInit = AppExecutorUtil.getAppExecutorService().submit(() -> {
            for (int attempt = 0; shouldTryStartingBinary(attempt); attempt++) {
                try {
                    binaryProcessGateway.init(binaryRun.generateRunCommand(singletonMap("ide-restart-counter", consecutiveRestarts)));

                    break;
                } catch (IOException | NoValidBinaryToRunException e) {
                    Logger.getInstance(getClass()).warn("Error restarting TabNine. Will try again.", e);

                    try {
                        sleepUponFailure(attempt);
                    } catch (InterruptedException e2) {
                        Logger.getInstance(getClass()).warn("TabNine was interrupted between restart attempts.", e);
                    }
                }
            }
        });
    }
}
