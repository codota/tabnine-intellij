package com.tabnine;

import static com.tabnine.plugin.InlineCompletionDriverKt.mockedApplicationWhichInvokesImmediately;
import static com.tabnine.testUtils.TestData.A_TEST_TXT_FILE;
import static com.tabnine.testUtils.TestData.SOME_CONTENT;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.tabnine.binary.BinaryProcessGateway;
import com.tabnine.binary.BinaryProcessGatewayProvider;
import com.tabnine.binary.BinaryRun;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.capabilities.SuggestionsModeService;
import com.tabnine.general.CompletionsEventSender;
import com.tabnine.general.DependencyContainer;
import com.tabnine.testUtils.TestData;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;

public abstract class MockedBinaryCompletionTestCase
    extends LightPlatformCodeInsightFixture4TestCase implements Disposable {
  private static int TESTS_TIMEOUTS_THRESHOLD_MILLIS = 500;
  protected static BinaryProcessGateway binaryProcessGatewayMock =
      Mockito.mock(BinaryProcessGateway.class);
  protected static BinaryRun binaryRunMock = Mockito.mock(BinaryRun.class);
  protected static BinaryProcessGatewayProvider binaryProcessGatewayProviderMock =
      Mockito.mock(BinaryProcessGatewayProvider.class);
  protected static SuggestionsModeService suggestionsModeServiceMock =
      Mockito.mock(SuggestionsModeService.class);
  protected static CompletionsEventSender completionEventSenderMock =
      Mockito.mock(CompletionsEventSender.class);

  @BeforeClass
  public static void setUpClass() {
    DependencyContainer.setTesting(
        binaryRunMock,
        binaryProcessGatewayProviderMock,
        suggestionsModeServiceMock,
        completionEventSenderMock,
        TESTS_TIMEOUTS_THRESHOLD_MILLIS);
  }

  @Before
  public void registerImmediateApplication() {
    ApplicationManager.setApplication(mockedApplicationWhichInvokesImmediately(), this);
  }

  @After
  public void postFixtureSetup() {
    Mockito.reset(
        binaryProcessGatewayMock,
        binaryRunMock,
        binaryProcessGatewayProviderMock,
        suggestionsModeServiceMock,
        completionEventSenderMock);

    Disposer.dispose(this);
  }

  @Override
  protected void setUp() throws Exception {
    preFixtureSetup();
    super.setUp();
    myFixture.configureByText(A_TEST_TXT_FILE, SOME_CONTENT);
  }

  @Override
  public void dispose() {}

  public void preFixtureSetup() throws Exception {
    when(binaryProcessGatewayMock.isDead()).thenReturn(false);
    when(binaryProcessGatewayProviderMock.generateBinaryProcessGateway())
        .thenReturn(binaryProcessGatewayMock);
    when(binaryRunMock.generateRunCommand(any())).thenReturn(singletonList(TestData.A_COMMAND));
    when(suggestionsModeServiceMock.getSuggestionMode()).thenReturn(SuggestionsMode.AUTOCOMPLETE);
  }

  protected void selectItem(LookupElement item) {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    selectItem(item, (char) 0);
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

  protected List<String> getPopupCompletions() {
    return Arrays.stream(myFixture.completeBasic())
        .map(LookupElement::getLookupString)
        .collect(toList());
  }
}
