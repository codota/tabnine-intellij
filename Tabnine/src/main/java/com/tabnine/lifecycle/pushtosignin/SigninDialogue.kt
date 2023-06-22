package com.tabnine.lifecycle.pushtosignin

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

class SigninDialogue : DialogWrapper(false) {
    private val title: JTextArea = JTextArea("Please sign in to start using Tabnine")

    init {
        title.isEditable = false
        title.background = Color.RED
        title.isOpaque = false
        init()
    }
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(title)
        return panel
    }
}
