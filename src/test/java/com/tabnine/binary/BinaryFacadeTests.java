package com.tabnine.binary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BinaryFacadeTests {
    @Mock
    private BinaryRun binaryRun;
    @InjectMocks
    private BinaryFacade binaryFacade;

    @Test
    public void whenCreateThenBinaryRunIsInit() throws IOException, NoValidBinaryToRunException {
        when(binaryRun.getBinaryRunCommand()).thenReturn(singletonList("echo"));

        binaryFacade.create();

        verify(binaryRun).init();
    }
}
