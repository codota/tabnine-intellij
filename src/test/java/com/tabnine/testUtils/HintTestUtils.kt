package com.tabnine.plugin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import java.awt.Point
import java.awt.event.MouseEvent

fun getCurrentPosition(editor: Editor): Point {
    return editor.offsetToXY(editor.caretModel.offset)
}

fun aCursorMoveEvent(editor: Editor, position: Point): EditorMouseEvent {
    val mouseEvent = MouseEvent(
        editor.component,
        1,
        System.currentTimeMillis(),
        0,
        position.x,
        position.y,
        0,
        false
    )

    return EditorMouseEvent(editor, mouseEvent, EditorMouseEventArea.EDITING_AREA)
}
