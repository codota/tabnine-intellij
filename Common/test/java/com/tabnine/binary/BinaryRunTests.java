package com.tabnine-common.binary;

import static com.tabnine.general.StaticConfig.UNINSTALLING_FLAG;
import static com.tabnine.testUtils.TabnineMatchers.hasItemInPosition;
import static com.tabnine.testUtils.TestData.A_NON_EXISTING_BINARY_PATH;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.tabnine.binary.fetch.BinaryVersionFetcher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BinaryRunTests {
  @Mock private BinaryVersionFetcher binaryVersionFetcher;
  @InjectMocks private BinaryRun binaryRun;

  @Test
  public void givenInitWhenGetBinaryRunCommandThenCommandFromFetchBinaryReturned()
      throws Exception {
    when(binaryVersionFetcher.fetchBinary()).thenReturn(A_NON_EXISTING_BINARY_PATH);

    assertThat(
        binaryRun.generateRunCommand(null),
        hasItemInPosition(0, equalTo(A_NON_EXISTING_BINARY_PATH)));
  }

  @Test
  public void givenBinaryIsEchoWhenReportingUninstallThenBinaryStartedWithUninstallFlag()
      throws Exception {
    when(binaryVersionFetcher.fetchBinary()).thenReturn("echo");

    Process process = binaryRun.reportUninstall(null);

    assertThat(
        new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
            .readLine(),
        equalTo(UNINSTALLING_FLAG));
  }
}
