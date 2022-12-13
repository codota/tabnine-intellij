package com.tabnine.binary;

import static com.tabnine.general.StaticConfig.*;
import static java.util.Collections.singletonMap;

import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.tabnine.binary.exceptions.BinaryCannotRecoverException;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class BinaryProcessRequesterProvider {
  private final BinaryRun binaryRun;
  private final BinaryProcessGatewayProvider binaryProcessGatewayProvider;

  private int consecutiveRestarts = 0;
  private Long firstTimeoutTimestamp = null;
  private BinaryProcessRequester binaryProcessRequester;
  private Future<?> binaryInit;
  private AtomicInteger requestsCounter = new AtomicInteger(0);
  private final int timeoutsThresholdMillis;
  private final int restartsThreshold;

  private BinaryProcessRequesterProvider(
      BinaryRun binaryRun,
      BinaryProcessGatewayProvider binaryProcessGatewayProvider,
      int timeoutsThresholdMillis,
      int restartsThreshold) {
    this.binaryRun = binaryRun;
    this.binaryProcessGatewayProvider = binaryProcessGatewayProvider;
    this.timeoutsThresholdMillis = timeoutsThresholdMillis;
    this.restartsThreshold = restartsThreshold;
  }

  public static BinaryProcessRequesterProvider create(
      BinaryRun binaryRun,
      BinaryProcessGatewayProvider binaryProcessGatewayProvider,
      int timeoutsThreshold,
      int restartsThreshold) {
    Logger.getInstance(BinaryProcessRequesterProvider.class)
        .debug(String.format("<<ALPHA LOG>> %s", "Creating binary process requester provider"));
    BinaryProcessRequesterProvider binaryProcessRequesterProvider =
        new BinaryProcessRequesterProvider(
            binaryRun, binaryProcessGatewayProvider, timeoutsThreshold, restartsThreshold);

    binaryProcessRequesterProvider.createNew();

    return binaryProcessRequesterProvider;
  }

  public BinaryProcessRequester get() {
    if (isStarting()) {
      Logger.getInstance(getClass())
          .info("Can't get completions because Tabnine process is not started yet.");

      return VoidBinaryProcessRequester.instance();
    }

    return binaryProcessRequester;
  }

  public void onSuccessfulRequest() {
    String msg =
        String.format(
            "Called successfulRequest with request #%d", requestsCounter.incrementAndGet());
    Logger.getInstance(BinaryProcessRequesterProvider.class)
        .debug(String.format("<<ALPHA LOG>> %s", msg));
    firstTimeoutTimestamp = null;
    consecutiveRestarts = 0;
  }

  public void onDead(Throwable e) {
    String msg = String.format("Called onDead with request #%d", requestsCounter.incrementAndGet());
    Logger.getInstance(BinaryProcessRequesterProvider.class)
        .debug(String.format("<<ALPHA LOG>> %s", msg));

    firstTimeoutTimestamp = null;
    Logger.getInstance(getClass()).warn("Tabnine is in invalid state, it is being restarted.", e);

    if (++consecutiveRestarts > restartsThreshold) {
      // NOTICE: In the production version of IntelliJ, logging an error kills the plugin. So this
      // is similar to exit(1);
      Logger.getInstance(getClass())
          .error(
              "Tabnine is not able to function properly. Contact support@tabnine.com",
              new BinaryCannotRecoverException());
    } else {
      createNew();
    }
  }

  public void onTimeout() {
    String msg =
        String.format("Called onTimeout with request #%d", requestsCounter.incrementAndGet());
    Logger.getInstance(BinaryProcessRequesterProvider.class)
        .debug(String.format("<<ALPHA LOG>> %s", msg));

    Logger.getInstance(getClass()).info("TabNine's response timed out.");

    long now = System.currentTimeMillis();
    if (firstTimeoutTimestamp == null) {
      firstTimeoutTimestamp = now;
    }

    if (now - firstTimeoutTimestamp >= timeoutsThresholdMillis) {
      Logger.getInstance(getClass())
          .warn(
              "Requests to TabNine's binary are consistently taking too long. Restarting the binary.");
      createNew();
    }
  }

  private boolean isStarting() {
    return this.binaryInit == null || !this.binaryInit.isDone();
  }

  private void createNew() {
    if (binaryProcessRequester != null) {
      binaryProcessRequester.destroy();
    }
    BinaryProcessGateway binaryProcessGateway =
        binaryProcessGatewayProvider.generateBinaryProcessGateway();

    initProcess(binaryProcessGateway);

    this.binaryProcessRequester =
        new BinaryProcessRequesterImpl(
            new ParsedBinaryIO(new GsonBuilder().create(), binaryProcessGateway));
  }

  private void initProcess(BinaryProcessGateway binaryProcessGateway) {
    binaryInit =
        AppExecutorUtil.getAppExecutorService()
            .submit(
                () -> {
                  for (int attempt = 0; shouldTryStartingBinary(attempt); attempt++) {
                    try {
                      binaryProcessGateway.init(
                          binaryRun.generateRunCommand(
                              singletonMap("ide-restart-counter", consecutiveRestarts)));

                      break;
                    } catch (IOException | NoValidBinaryToRunException e) {
                      Logger.getInstance(getClass())
                          .warn("Error restarting TabNine. Will try again.", e);

                      try {
                        sleepUponFailure(attempt);
                      } catch (InterruptedException e2) {
                        Logger.getInstance(getClass())
                            .warn("TabNine was interrupted between restart attempts.", e);
                      }
                    }
                  }
                });
  }
}
