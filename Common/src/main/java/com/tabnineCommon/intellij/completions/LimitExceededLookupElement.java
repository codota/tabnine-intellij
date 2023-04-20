package com.tabnineCommon.intellij.completions;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.tabnineCommon.hover.HoverUpdater;
import org.jetbrains.annotations.NotNull;

/**
 * This is a lookup element for locked completions. Locked completions are completions that appear
 * when the user already exceeded the completions daily quota. The completion text appears as usual
 * and its type text contains a lock symobl. When selecting such a completion no text is inserted
 * and instead, a gray inlay is shown, with a message indicating the quota was exceeded. Hovering
 * the mouse over this inlay open a balloon with more details and possibly CTA links.
 */
public class LimitExceededLookupElement extends LookupElementDecorator<LookupElement>
    implements LookupListener {
  private final HoverUpdater hoverUpdater = new HoverUpdater();

  protected LimitExceededLookupElement(LookupElement delegate) {
    super(delegate);
  }

  @Override
  public void itemSelected(@NotNull LookupEvent lookupEvent) {
    if (lookupEvent.getItem() != this) {
      return; // handle only selection of this item.
    }
    this.hoverUpdater.update(lookupEvent.getLookup().getEditor());
  }
}
