package com.tabnine.inline;

import com.tabnine.prediction.TabNineCompletion;
import java.util.List;

public interface OnCompletionFetchedCallback {
  void onCompletionFetched(List<TabNineCompletion> completions);
}
