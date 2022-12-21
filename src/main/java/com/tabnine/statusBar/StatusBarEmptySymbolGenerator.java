package com.tabnine.statusBar;

import java.util.Arrays;
import java.util.List;

// Workaround to address the issue where IJ does not render the width of the status bar icon correctly when the icon changes
// because it doesn't detect a change in the widget text. (until document change occurs)
// Creating a different empty text each time will solve the problem.
public class StatusBarEmptySymbolGenerator {
  private static final String EMPTY_SYMBOL = "\u0000";
  private static final List<String> EMPTY_SYMBOLS =
      Arrays.asList(EMPTY_SYMBOL, EMPTY_SYMBOL + EMPTY_SYMBOL);
  private int currentSymbolIndex = 0;

  public String getEmptySymbol() {
    return EMPTY_SYMBOLS.get(currentSymbolIndex++ % EMPTY_SYMBOLS.size());
  }
}
