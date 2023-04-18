package com.tabnine.intellij.completions;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import org.jetbrains.annotations.NotNull;

public class InsertNothingLookupElement extends LookupElementDecorator<LookupElement>
    implements LookupListener {
  private final String prefix;
  private boolean selected;

  protected InsertNothingLookupElement(LookupElement delegate, String prefix) {
    super(delegate);
    this.prefix = prefix;
  }

  @NotNull
  @Override
  public String getLookupString() {
    if (!this.selected) {
      // When the item is selected, we don't want to insert its lookup string into the file, so we
      // return an empty lookup string.
      return super.getLookupString();
    } else {
      return prefix;
    }
  }

  @Override
  public void lookupCanceled(@NotNull LookupEvent event) {
    // backward compatibility with IJ 2017.1
  }

  @Override
  public void currentItemChanged(LookupEvent event) {
    // It would make more sense to do this in beforeItemSelected method, but that method doesn't
    // exist in older
    // IJ versions. So, once the popup is displayed, we can actually change this completion lookup
    // string
    // and it will only affect the text that is inserted when selecting it (i.e. insert nothing).
    if (event.getItem() == this) {
      this.selected = true;
    }
  }
}
