package com.tabnine.userSettings

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import java.awt.Color
import javax.swing.JColorChooser
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class AppSettingsComponent {
    val panel: JPanel
    private val colorChooser = JColorChooser()

    val preferredFocusedComponent: JComponent
        get() = colorChooser

    var chosenColor: Int
        get() = colorChooser.color.rgb
        set(colorRGB) {
            colorChooser.color = Color(colorRGB)
        }

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Inline Hint Color:", UIUtil.ComponentStyle.LARGE), colorChooser, 1, true)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}
