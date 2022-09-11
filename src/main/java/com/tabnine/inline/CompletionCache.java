package com.tabnine.inline;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import com.tabnine.prediction.TabNineCompletion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompletionCache {
  private static final Key<List<TabNineCompletion>> TABNINE_COMPLETION_CACHE =
      Key.create("TABNINE_COMPLETION_CACHE");

  public CompletionCache() {}

  public List<TabNineCompletion> get(Editor editor) {
    return TABNINE_COMPLETION_CACHE.get(editor);
  }

  public void set(Editor editor, List<TabNineCompletion> completions) {
    TABNINE_COMPLETION_CACHE.set(editor, completions);
  }
}
