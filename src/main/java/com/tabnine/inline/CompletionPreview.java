package com.tabnine.inline;

import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;
import com.intellij.ui.JBColor;
import com.intellij.util.containers.FList;
import com.tabnine.capabilities.SuggestionsMode;
import com.tabnine.general.DependencyContainer;
import com.tabnine.prediction.TabNineCompletion;
import com.tabnine.selections.AutoImporter;
import com.tabnine.selections.CompletionPreviewListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompletionPreview implements Disposable {

  private static final Key<CompletionPreview> INLINE_COMPLETION_PREVIEW =
      Key.create("INLINE_COMPLETION_PREVIEW");

  private final CompletionPreviewListener previewListener =
      DependencyContainer.instanceOfCompletionPreviewListener();
  private final Editor editor;
  private final PsiFile file;
  private List<TabNineCompletion> completions;
  private int previewIndex;
  private String suffix;
  private Inlay inlay;
  private final KeyListener previewKeyListener = new PreviewKeyListener();

  private CompletionPreview(@NotNull Editor editor, @NotNull PsiFile file) {
    this.editor = editor;
    this.file = file;
  }

  @Nullable
  String updatePreview(@NotNull List<TabNineCompletion> completions, int previewIndex, int offset) {
    if (SuggestionsMode.getSuggestionMode() != SuggestionsMode.INLINE) {
      return null;
    }
    this.completions = completions;
    this.previewIndex = previewIndex;
    TabNineCompletion completion = completions.get(previewIndex);
    if (inlay != null) {
      Disposer.dispose(inlay);
      inlay = null;
    }
    suffix = getSuffixText(completion);

    if (!suffix.isEmpty()
        && editor instanceof EditorImpl
        && !editor.getSelectionModel().hasSelection()
        && InplaceRefactoring.getActiveInplaceRenamer(editor) == null) {
      inlay =
          editor
              .getInlayModel()
              .addInlineElement(offset, true, createGrayRenderer(suffix, completion.deprecated));
      if (inlay != null) {
        Disposer.register(this, inlay);
        editor.getContentComponent().addKeyListener(previewKeyListener);
      }
    }
    return suffix;
  }

  void clear() {
    editor.getContentComponent().removeKeyListener(previewKeyListener);
    if (inlay != null) {
      Disposer.dispose(inlay);
      inlay = null;
    }
    completions = null;
    suffix = null;
  }

  private String getSuffixText(@NotNull TabNineCompletion completion) {
    String itemText = completion.newPrefix + completion.newSuffix;
    String prefix = completion.completionPrefix;
    if (prefix.isEmpty()) {
      return itemText;
    }

    FList<TextRange> fragments = LookupCellRenderer.getMatchingFragments(prefix, itemText);
    if (fragments != null && !fragments.isEmpty()) {
      List<TextRange> list = new ArrayList<>(fragments);
      return itemText.substring(list.get(list.size() - 1).getEndOffset());
    }
    return "";
  }

  @NotNull
  private EditorCustomElementRenderer createGrayRenderer(final String suffix, boolean deprecated) {
    return new EditorCustomElementRenderer() {
      @Override
      public int calcWidthInPixels(@NotNull Inlay inlay) {
        return editor.getContentComponent().getFontMetrics(getFont(editor)).stringWidth(suffix);
      }

      @Override
      public void paint(
          @NotNull Inlay inlay,
          @NotNull Graphics g,
          @NotNull Rectangle targetRegion,
          @NotNull TextAttributes textAttributes) {
        g.setColor(JBColor.GRAY);
        g.setFont(getFont(editor));
        g.drawString(suffix, targetRegion.x, targetRegion.y + ((EditorImpl) editor).getAscent());
      }

      private Font getFont(@NotNull Editor editor) {
        Font font = editor.getColorsScheme().getFont(EditorFontType.PLAIN);
        if (!deprecated) {
          return font;
        }
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        return new Font(attributes);
      }
    };
  }

  @Override
  public void dispose() {
    clear();
    editor.putUserData(INLINE_COMPLETION_PREVIEW, null);
    CompletionState.clearCompletionState(editor);
  }

  private void applyPreview() {
    WriteCommandAction.runWriteCommandAction(
        file.getProject(),
        "Tabnine Inline Completion",
        null,
        () -> {
          TabnineDocumentListener.mute();
          try {
            int startOffset =
                inlay.getOffset() - completions.get(previewIndex).completionPrefix.length();
            int endOffset = inlay.getOffset() + suffix.length();
            editor.getDocument().insertString(inlay.getOffset(), suffix);
            editor.getCaretModel().moveToOffset(endOffset);
            AutoImporter.registerTabNineAutoImporter(
                editor, file.getProject(), startOffset, endOffset);
            previewListener.previewSelected(
                new CompletionPreviewListener.CompletionPreviewData(
                    completions, previewIndex, file));
            Disposer.dispose(CompletionPreview.this);
          } finally {
            TabnineDocumentListener.unmute();
          }
        });
  }

  static CompletionPreview findOrCreateCompletionPreview(
      @NotNull Editor editor, @NotNull PsiFile file) {
    CompletionPreview preview = editor.getUserData(INLINE_COMPLETION_PREVIEW);
    if (preview == null) {
      preview = new CompletionPreview(editor, file);
      EditorUtil.disposeWithEditor(editor, preview);
      editor.putUserData(INLINE_COMPLETION_PREVIEW, preview);
    }
    return preview;
  }

  @TestOnly
  public static String getPreviewText(@NotNull Editor editor) {
    CompletionPreview preview = editor.getUserData(INLINE_COMPLETION_PREVIEW);
    if (preview != null) {
      return preview.suffix;
    }
    return null;
  }

  private class PreviewKeyListener extends KeyAdapter {
    @Override
    public void keyReleased(KeyEvent event) {
      try {
        if (CompletionPreview.this.inlay == null) {
          return;
        }
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE || event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
          Disposer.dispose(CompletionPreview.this);
          return;
        }
        if (event.getKeyCode() != KeyEvent.VK_RIGHT) {
          return;
        }
        applyPreview();
      } catch (Throwable err) {
        Logger.getInstance(getClass()).warn("Error in Tabnine preview KeyListener", err);
      }
    }
  }
}
