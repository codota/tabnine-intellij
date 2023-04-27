package com.tabnine.binary;

import static com.tabnineCommon.general.StaticConfig.UNINSTALLING_FLAG;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.tabnine.testUtils.TabnineMatchers;
import com.tabnine.testUtils.TestData;
import com.tabnineCommon.binary.BinaryRun;
import com.tabnineCommon.binary.fetch.BinaryVersionFetcher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
    when(binaryVersionFetcher.fetchBinary()).thenReturn(TestData.A_NON_EXISTING_BINARY_PATH);

    List<String> command = binaryRun.generateRunCommand(null, null);
    assertThat(
        command,
        TabnineMatchers.hasItemInPosition(0, equalTo(TestData.A_NON_EXISTING_BINARY_PATH)));

    assertEquals("Should not add cloud2url param", command.size(), 1);
  }

  @Test
  public void givenBinaryIsEchoWhenReportingUninstallThenBinaryStartedWithUninstallFlag()
      throws Exception {
    when(binaryVersionFetcher.fetchBinary()).thenReturn("echo");

    Process process = binaryRun.reportUninstall(null, null);

    assertThat(
        new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
            .readLine(),
        equalTo(UNINSTALLING_FLAG));
  }

  @Test
  public void givenDifferentServerUrlItWillAddThatToRunCommand() throws Exception {
    when(binaryVersionFetcher.fetchBinary()).thenReturn(TestData.A_NON_EXISTING_BINARY_PATH);

    assertThat(
        binaryRun.generateRunCommand(null, "http://oh-no"),
        TabnineMatchers.hasItemInPosition(1, equalTo("--cloud2_url=http://oh-no")));
  }
}
