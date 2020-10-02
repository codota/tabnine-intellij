package com.tabnine.integration;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.tabnine.binary.BinaryFacade;
import com.tabnine.binary.TabNineProcessFacade;
import org.junit.Before;
import org.mockito.Mockito;

import static com.tabnine.testutils.TestData.A_TEST_TXT_FILE;
import static com.tabnine.testutils.TestData.SOME_CONTENT;
import static org.mockito.Mockito.when;

public abstract class MockedBinaryCompletionTestCase extends LightPlatformCodeInsightFixture4TestCase {
    protected BinaryFacade tabNineBinaryMock;

    @Before
    public void initChildProcessMock() {
        tabNineBinaryMock = Mockito.mock(BinaryFacade.class);

        TabNineProcessFacade.setTesting(tabNineBinaryMock);
        when(tabNineBinaryMock.isDead()).thenReturn(false);
        myFixture.configureByText(A_TEST_TXT_FILE, SOME_CONTENT);
    }
}
