package com.tabnine.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.tabnine.binary.BinaryFacade;
import com.tabnine.binary.TabNineProcessFacade;
import org.jetbrains.annotations.NotNull;
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

    protected void selectItem(LookupElement item) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        selectItem(item, (char)0);
    }

    protected void selectItem(@NotNull LookupElement item, final char completionChar) {
        final LookupImpl lookup = getLookup();
        lookup.setCurrentItem(item);
        if (LookupEvent.isSpecialCompletionChar(completionChar)) {
            lookup.finishLookup(completionChar);
        } else {
            type(completionChar);
        }
    }

    protected LookupImpl getLookup() {
        return (LookupImpl) LookupManager.getInstance(getProject()).getActiveLookup();
    }

    protected void type(char c) {
        myFixture.type(c);
    }

    protected void type(String s) {
        myFixture.type(s);
    }
}
