package com.tabnine.state;

import static com.tabnine.general.Utils.getDaysDiff;

import com.intellij.ide.util.PropertiesComponent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CompletionHintState {
  private static final int MAX_DAYS_TO_SHOW_COMPLETION_HINT = 3;
  private static final String IS_COMPLETION_HINT_STORAGE_KEY = "completion-hint-tooltip";
  private Date installationTime;

  public CompletionHintState(String installationTime) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    try {
      this.installationTime = formatter.parse(installationTime);
    } catch (ParseException e) {
      this.installationTime = null;
    }
  }

  public boolean isEligibleForCompletionHint() {
    if (!isCompletionHintShown()) {
      long daysDiff = getDaysDiff(new Date(), installationTime);
      return daysDiff >= 0 && daysDiff <= MAX_DAYS_TO_SHOW_COMPLETION_HINT;
    }
    return false;
  }

  public void setIsCompletionHintShown(boolean isShown) {
    PropertiesComponent.getInstance().setValue(IS_COMPLETION_HINT_STORAGE_KEY, isShown);
  }

  private boolean isCompletionHintShown() {
    return PropertiesComponent.getInstance().getBoolean(IS_COMPLETION_HINT_STORAGE_KEY, false);
  }
}
