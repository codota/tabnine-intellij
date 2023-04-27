package com.tabnine.binary.fetch;

import static com.tabnineCommon.general.StaticConfig.BINARY_MINIMUM_REASONABLE_SIZE;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tabnine.testUtils.TabnineMatchers;
import com.tabnine.testUtils.TestData;
import com.tabnineCommon.binary.exceptions.FailedToDownloadException;
import com.tabnineCommon.binary.fetch.BinaryValidator;
import com.tabnineCommon.binary.fetch.TempBinaryValidator;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TempBinaryValidatorTests {
  @TempDir public Path temporaryFolder;
  @Mock private BinaryValidator binaryValidator;
  @InjectMocks private TempBinaryValidator tempBinaryValidator;

  @Test
  public void givenTempBinaryNotExistsWhenValidatedThenFailedToDownloadExceptionThrown()
      throws Exception {
    assertThrows(
        FailedToDownloadException.class,
        () -> tempBinaryValidator.validateAndRename(aTempFile(), aDestinationFile()));
  }

  @Test
  public void
      givenTempBinaryWithContentSmallerThanThresholdWhenValidatedThenFailedToDownloadExceptionThrown()
          throws Exception {
    Files.write(
        aTempFile(),
        TestData.binaryContentSized(BINARY_MINIMUM_REASONABLE_SIZE - TestData.EPSILON));

    assertThrows(
        FailedToDownloadException.class,
        () -> tempBinaryValidator.validateAndRename(aTempFile(), aDestinationFile()));
  }

  @Test
  public void givenTempBinaryNoWorkingWhenValidatedThenFailedToDownloadExceptionThrown()
      throws Exception {
    Files.write(
        aTempFile(),
        TestData.binaryContentSized(BINARY_MINIMUM_REASONABLE_SIZE + TestData.EPSILON));
    when(binaryValidator.isWorking(aTempFile().toString())).thenReturn(false);

    assertThrows(
        FailedToDownloadException.class,
        () -> tempBinaryValidator.validateAndRename(aTempFile(), aDestinationFile()));
  }

  @Test
  public void givenValidTempBinaryWhenValidatedThenItIsMovedToDestination() throws Exception {
    Files.write(
        aTempFile(),
        TestData.binaryContentSized(BINARY_MINIMUM_REASONABLE_SIZE + TestData.EPSILON));
    when(binaryValidator.isWorking(aTempFile().toString())).thenReturn(true);

    tempBinaryValidator.validateAndRename(aTempFile(), aDestinationFile());

    assertThat(
        aDestinationFile().toFile(),
        TabnineMatchers.fileContentEquals(
            TestData.binaryContentSized(BINARY_MINIMUM_REASONABLE_SIZE + TestData.EPSILON)));
  }

  @Test
  public void whenTempBinaryValidatedThenItIsSetAsExecutable() throws Exception {
    Path mockedTempDestination = mock(Path.class);
    File mockedTempFile = mock(File.class);
    when(mockedTempDestination.toFile()).thenReturn(mockedTempFile);
    when(mockedTempFile.exists()).thenReturn(true);
    when(mockedTempFile.length())
        .thenReturn((long) (BINARY_MINIMUM_REASONABLE_SIZE + TestData.EPSILON));

    try {
      tempBinaryValidator.validateAndRename(mockedTempDestination, aDestinationFile());
    } catch (Exception ignored) {
    }

    verify(mockedTempFile).setExecutable(true);
  }

  private Path aDestinationFile() {
    return Paths.get(temporaryFolder.toString(), "destinationfile");
  }

  private Path aTempFile() {
    return Paths.get(temporaryFolder.toString(), "tempfile");
  }
}
