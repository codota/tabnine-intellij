package com.tabnine.testUtils;

import static com.tabnine.general.StaticConfig.CONSECUTIVE_RESTART_THRESHOLD;
import static com.tabnine.general.StaticConfig.ILLEGAL_RESPONSE_THRESHOLD;
import static com.tabnine.testUtils.TestData.*;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class BadResultsUtils {
  @NotNull
  public static Stream<String> overThresholdBadResultsWithAGoodResultInBetween() {
    Stream<String> results =
        Stream.concat(enoughBadResultsToCauseARestart(), Stream.of(A_PREDICTION_RESULT));

    for (int i = 0; i < CONSECUTIVE_RESTART_THRESHOLD; i++) {
      results = Stream.concat(results, enoughBadResultsToCauseARestart());
    }

    return results;
  }

  @NotNull
  public static Stream<String> enoughBadResultsToCauseADeath() {
    Stream<String> results = enoughBadResultsToCauseARestart();

    for (int i = 0; i < CONSECUTIVE_RESTART_THRESHOLD; i++) {
      results = Stream.concat(results, enoughBadResultsToCauseARestart());
    }

    return results;
  }

  private static Stream<String> enoughBadResultsToCauseARestart() {
    return IntStream.range(0, ILLEGAL_RESPONSE_THRESHOLD + OVERFLOW).mapToObj(i -> INVALID_RESULT);
  }
}
