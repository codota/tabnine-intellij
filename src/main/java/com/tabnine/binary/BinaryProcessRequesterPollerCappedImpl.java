package com.tabnine.binary;

import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.requests.config.StateRequest;
import com.tabnine.binary.requests.config.StateResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.intellij.util.concurrency.AppExecutorUtil.getAppExecutorService;
import static com.tabnine.general.StaticConfig.wrapWithBinaryRequest;

public class BinaryProcessRequesterPollerCappedImpl implements BinaryProcessRequesterPoller  {

    // number of times it will poll until ready
    public int pollAttempts;
    // sleep time between poll attempts to binary
    public int pollSleepIntervalMillis;

    public int requestTimeoutMillis;

    public BinaryProcessRequesterPollerCappedImpl(int pollAttempts, int pollSleepIntervalMillis, int requestTimeoutMillis) {
        this.pollAttempts = pollAttempts;
        this.pollSleepIntervalMillis = pollSleepIntervalMillis;
        this.requestTimeoutMillis = requestTimeoutMillis;
    }


    @Override
    public void pollUntilReady(BinaryProcessGateway binaryProcessGateway) throws TabNineDeadException {
        if (this.pollAttempts <= 0) {
            return;
        }

        ParsedBinaryIO parsedBinaryIO = new ParsedBinaryIO(new GsonBuilder().create(), binaryProcessGateway);
        int remaining = this.pollAttempts;
        long startTime = System.currentTimeMillis();
        while (remaining > 0) {
            remaining--;
            StateRequest stateRequest = new StateRequest();
            try {

                StateResponse stateResponse = getAppExecutorService()
                        .submit(() -> makeStateRequest(parsedBinaryIO, stateRequest))
                        .get(this.requestTimeoutMillis, TimeUnit.MILLISECONDS);
                if (stateResponse != null) {
                    long end = System.currentTimeMillis();
                    Logger.getInstance(getClass()).info("polling took " + (end - startTime) + "ms");
                    return;
                }
            } catch (Exception e) {
                // debug log this since it can re-try and don't want to send to sentry
                Logger.getInstance(getClass()).debug("polling failed with error");
                System.out.println(e);
            }
            try {
                Thread.sleep(pollSleepIntervalMillis);
            } catch (InterruptedException e) {

            }
        }

        TabNineDeadException exception = new TabNineDeadException("binary polling failed");
        // this will be sent to sentry
        Logger.getInstance(getClass()).warn("binary polling failed", exception);
        throw  exception;
    }

    private StateResponse makeStateRequest(ParsedBinaryIO parsedBinaryIO, StateRequest stateRequest) throws IOException, com.tabnine.binary.exceptions.TabNineDeadException, com.tabnine.binary.exceptions.TabNineInvalidResponseException {
        parsedBinaryIO.writeRequest(wrapWithBinaryRequest(stateRequest.serialize()));
        return parsedBinaryIO.readResponse(stateRequest.response());
    }
}
