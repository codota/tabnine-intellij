package com.tabnine.binary;

import com.tabnine.binary.fetch.BinaryVersionFetcher;
import com.tabnine.testutils.TabnineMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.tabnine.testutils.TestData.A_NON_EXISTING_BINARY_PATH;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BinaryRunTests {
    @Mock
    private BinaryVersionFetcher binaryVersionFetcher;
    @InjectMocks
    private BinaryRun binaryRun;

    @Test
    public void givenInitWhenGetBinaryRunCommandThenCommandFromFetchBinaryReturned() throws Exception {
        when(binaryVersionFetcher.fetchBinary()).thenReturn(A_NON_EXISTING_BINARY_PATH);

        assertThat(binaryRun.getBinaryRunCommand(), TabnineMatchers.hasItemInPosition(0, equalTo(A_NON_EXISTING_BINARY_PATH)));
    }
}
