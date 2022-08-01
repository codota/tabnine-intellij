package com.tabnine.state;

import static com.tabnine.general.Utils.getDaysDiff;

import com.intellij.ide.util.PropertiesComponent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SuggestionHintState {
  private static final int MAX_DAYS_TO_SHOW_SUGGESTION_HINT = 3;
  private static final String IS_SUGGESTION_HINT_SHOWN_STORAGE_KEY = "suggestion-hint-tooltip";
  private Date installationTime;

  public SuggestionHintState(String installationTime) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    try {
      this.installationTime = formatter.parse(installationTime);
    } catch (ParseException e) {
      this.installationTime = null;
    }
  }

  public boolean isEligibleForSuggestionHint() {
    if (isHintShown()) {
      return false;
    }
    long daysDiff = getDaysDiff(new Date(), installationTime);
    return daysDiff >= 0 && daysDiff <= MAX_DAYS_TO_SHOW_SUGGESTION_HINT;
  }

  public void setHintWasShown() {
    if (isHintShown()) {
      return;
    }
    PropertiesComponent.getInstance().setValue(IS_SUGGESTION_HINT_SHOWN_STORAGE_KEY, true);
  }

  private boolean isHintShown() {
    return PropertiesComponent.getInstance()
        .getBoolean(IS_SUGGESTION_HINT_SHOWN_STORAGE_KEY, false);
  }
}
