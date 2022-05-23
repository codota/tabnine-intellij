package com.tabnine.inline

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.util.Alarm
import com.tabnine.inline.render.TabnineInlay
import java.awt.Component
import java.awt.Point
import javax.swing.SwingUtilities

private const val HINT_DELAY_MS = 100

class CompletionPreviewInsertionHint(
    private val editor: Editor,
    private val inlay: TabnineInlay,
    private var suffix: String = ""
) : Disposable,
    EditorMouseMotionListener {
    private var alarm: Alarm = Alarm(this)

    init {
        editor.addEditorMouseMotionListener(this)
    }

    override fun mouseMoved(e: EditorMouseEvent) {
        alarm.cancelAllRequests()

        if (inlay.isEmpty || e.area !== EditorMouseEventArea.EDITING_AREA) {
            return
        }

        val mouseEvent = e.mouseEvent
        val point = mouseEvent.point

        if (!isOverPreview(point)) {
            return
        }

        alarm.addRequest(
            {
                InlineHints.showPreInsertionHint(
                    editor,
                    SwingUtilities.convertPoint(
                        mouseEvent.source as Component,
                        point,
                        editor.component.rootPane.layeredPane
                    )
                )
            },
            HINT_DELAY_MS
        )
    }

    override fun dispose() {
        editor.removeEditorMouseMotionListener(this)
    }

    fun updateSuffix(suffix: String?) {
        this.suffix = suffix ?: ""
    }

    private fun isOverPreview(p: Point): Boolean {
        return inlay.getBounds()?.contains(p) ?: isLogicallyInsideInlay(p)
    }

    private fun isLogicallyInsideInlay(p: Point): Boolean {
        val pos: LogicalPosition = editor.xyToLogicalPosition(p)

        if (pos.line >= editor.document.lineCount) {
            return false
        }

        val pointOffset = editor.logicalPositionToOffset(pos)
        val inlayOffset = inlay.offset ?: return false

        return pointOffset >= inlayOffset && pointOffset <= inlayOffset + suffix.length
    }
}
