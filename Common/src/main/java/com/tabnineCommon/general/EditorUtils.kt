package com.tabnineCommon.general

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorKind

object EditorUtils {

    @JvmStatic
    fun isMainEditor(editor: Editor): Boolean {
        return editor.editorKind == EditorKind.MAIN_EDITOR || ServiceManager.isUnitTestMode
    }
}
