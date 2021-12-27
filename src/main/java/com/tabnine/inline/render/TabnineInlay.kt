package com.tabnine.inline.render

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.tabnine.capabilities.CapabilitiesService
import com.tabnine.capabilities.Capability
import com.tabnine.inline.render.experimental.ExperimentalTabnineInlay
import com.tabnine.inline.render.preserved.DefaultTabnineInlay
import com.tabnine.prediction.TabNineCompletion
import java.awt.Rectangle

interface TabnineInlay {
    val offset: Int?
    val bounds: Rectangle?
    val isEmpty: Boolean
    fun register(parent: Disposable)
    fun clear()
    fun render(editor: Editor, suffix: String, completion: TabNineCompletion, offset: Int)

    companion object {
        @JvmStatic
        fun create(): TabnineInlay {
            val isAlpha = CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)

            return if (isAlpha) ExperimentalTabnineInlay() else DefaultTabnineInlay()
        }
    }
}
