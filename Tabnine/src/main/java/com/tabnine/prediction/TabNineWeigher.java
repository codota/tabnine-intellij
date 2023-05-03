package com.tabnine.prediction;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.tabnineCommon.prediction.TabNineCompletion;

public class TabNineWeigher extends LookupElementWeigher {
  public TabNineWeigher() {
    super("TabNineLookupElementWeigher", false, true);
  }

  @Override
  public Integer weigh(LookupElement element) {
    if (element.getObject() instanceof TabNineCompletion) {
      return ((TabNineCompletion) element.getObject()).index;
    }

    return Integer.MAX_VALUE;
  }
}
