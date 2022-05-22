package com.tabnine.inline.render

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.tabnine.prediction.TabNineCompletion
import java.awt.Rectangle

interface TabnineInlay {
    val offset: Int?
    val isEmpty: Boolean

    fun getBounds(): Rectangle?
    fun register(parent: Disposable)
    fun clear()
    fun render(editor: Editor, completion: TabNineCompletion, offset: Int)

    companion object {
        @JvmStatic
        fun create(): TabnineInlay {
            return DefaultTabnineInlay()
        }
    }
}
