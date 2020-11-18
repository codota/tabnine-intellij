package com.tabnine.binary.fetch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ExtendWith(MockitoExtension.class)
public class BinaryValidatorTests {
    @InjectMocks
    private BinaryValidator binaryValidator;

    @Test
    public void givenABinaryThatReturnsAResultWhenValidatingThenBinaryIsValid() throws Exception {
        assertThat(binaryValidator.isWorking("echo"), is(true));
    }

    @Test
    public void givenABinaryThatDoesNotReturnAResultWhenValidatingThenBinaryIsNotValid() throws Exception {
        assertThat(binaryValidator.isWorking("exit"), is(false));
    }

    @Test
    public void givenABinaryThatDoesNotExistWhenValidatingThenBinaryIsNotValid() throws Exception {
        assertThat(binaryValidator.isWorking("/test/test123"), is(false));
    }

}
