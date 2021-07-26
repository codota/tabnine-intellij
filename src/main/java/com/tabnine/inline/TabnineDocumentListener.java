package com.tabnine.inline;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TabnineDocumentListener implements DocumentListener {

    private CompletionPreview preview;

    private static final AtomicBoolean isMuted = new AtomicBoolean(false);

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        System.out.println("--> documentChanged with offset=" + event.getOffset() + ", newFragment=" + event.getNewFragment());
        if (isMuted.get() || ToggleCompletionModeAction.shouldUseCompletionMenu()) {
            return;
        }
        Document document = event.getDocument();
        if (ObjectUtils.doIfCast(document, DocumentEx.class, DocumentEx::isInBulkUpdate) == Boolean.TRUE) {
            return;
        }
        Editor editor = getActiveEditor(document);
        Project project = ObjectUtils.doIfNotNull(editor, Editor::getProject);
        PsiFile file = ObjectUtils.doIfNotNull(project, proj -> PsiDocumentManager.getInstance(proj).getPsiFile(document));
        if (editor == null || project == null || file == null) {
            return;
        }
        if (preview == null) {
            preview = CompletionPreview.findOrCreateCompletionPreview(editor, file);
        }
        InlineCompletionHandler handler = new InlineCompletionHandler(true);
        handler.invoke(project, editor, file, event.getOffset() + event.getNewLength());
    }

    public static void mute() {
        isMuted.set(true);
    }

    public static void unmute() {
        isMuted.set(false);
    }

    @Nullable
    private static Editor getActiveEditor(@NotNull Document document) {
        Component focusOwner = IdeFocusManager.getGlobalInstance().getFocusOwner();
        DataContext dataContext = DataManager.getInstance().getDataContext(focusOwner);
        // ignore caret placing when exiting
        Editor activeEditor = ApplicationManager.getApplication().isDisposed() ? null : CommonDataKeys.EDITOR.getData(dataContext);
        if (activeEditor != null && activeEditor.getDocument() != document) {
            activeEditor = null;
        }
        return activeEditor;
    }

//    private Optional<InlineCompletionParameters> buildInlineCompletionParameters(@NotNull DocumentEvent event) {
//        Document document = event.getDocument();
//        Editor editor = getActiveEditor(document);
//        Project project = ObjectUtils.doIfNotNull(editor, Editor::getProject);
//        PsiFile file = ObjectUtils.doIfNotNull(project, proj -> PsiDocumentManager.getInstance(proj).getPsiFile(document));
//        if (editor == null || project == null || file == null) {
//            return Optional.empty();
//        }
//        int newOffset = event.getOffset() + event.getNewFragment().length();
//        String documentText = document.getText();
//        if (newOffset < 0 || newOffset > documentText.length() - 1) {
//            return Optional.empty();
//        }
//        documentText = new StringBuilder(documentText).insert(newOffset, "TabnineDummy").toString();
//        file = PsiFileFactory.getInstance(project).createFileFromText("tmp-" + file.getName(), file.getFileType(), documentText);
//        Document copyDocument = file.getViewProvider().getDocument();
//        if (copyDocument == null) {
//            return Optional.empty();
//        }
////        PsiDocumentManager.getInstance(project).commitDocument(document);
//        PsiElement element = file.findElementAt(event.getOffset());
//        if (element == null) {
//            return Optional.empty();
//        }
//        System.out.println("--> current element is " + element);
//        String prefix = CompletionData.findPrefixStatic(element, event.getOffset());
//        System.out.println("--> prefix is " + prefix);
//    return Optional.of(
//        new InlineCompletionParameters(
//            editor,
//            project,
//            document,
//            prefix,
//            event.getNewFragment(),
//            event.getOffset() + event.getNewLength()));
//    }
}
