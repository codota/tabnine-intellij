package com.tabnineCommon.inline;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class InlineCompletionParameters {
  @NotNull private final Editor editor;
  @NotNull private final Project project;
  @NotNull private final Document document;
  @NotNull private final String prefix;
  @NotNull private final CharSequence newFragment;
  private final int offset;

  InlineCompletionParameters(
      @NotNull Editor editor,
      @NotNull Project project,
      @NotNull Document document,
      @NotNull String prefix,
      @NotNull CharSequence newFragment,
      int offset) {
    this.editor = editor;
    this.project = project;
    this.document = document;
    this.prefix = prefix;
    this.newFragment = newFragment;
    this.offset = offset;
  }

  @NotNull
  public Editor getEditor() {
    return editor;
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  @NotNull
  public Document getDocument() {
    return document;
  }

  @NotNull
  public String getPrefix() {
    return prefix;
  }

  public int getOffset() {
    return offset;
  }

  @NotNull
  public CharSequence getNewFragment() {
    return newFragment;
  }

  @Override
  public String toString() {
    return "InlineCompletionParameters{"
        + "editor="
        + editor
        + ", project="
        + project
        + ", document="
        + document
        + ", prefix='"
        + prefix
        + '\''
        + ", newFragment="
        + newFragment
        + ", offset="
        + offset
        + '}';
  }
}
