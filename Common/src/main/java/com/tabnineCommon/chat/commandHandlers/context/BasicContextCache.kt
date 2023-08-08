package com.tabnineCommon.chat.commandHandlers.context

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key

object BasicContextCache {
    private val fileMetadataKey = Key.create<BasicContext>("com.tabnine.FileMetadataCacheEntry")
    fun save(editor: Editor, entry: BasicContext) = editor.putUserData(fileMetadataKey, entry)

    fun get(editor: Editor) = editor.getUserData(fileMetadataKey)
}
