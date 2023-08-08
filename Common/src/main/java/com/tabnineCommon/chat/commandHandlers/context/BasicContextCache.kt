package com.tabnineCommon.chat.commandHandlers.context

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key

object BasicContextCache {
    private val basicContextKey = Key.create<BasicContext>("com.tabnine.BasicContextCacheKey")
    fun save(editor: Editor, entry: BasicContext) = editor.putUserData(basicContextKey, entry)

    fun get(editor: Editor) = editor.getUserData(basicContextKey)
}
