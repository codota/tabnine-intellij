package com.tabnine.userSettings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
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
    private val logFilePathComponent = JBTextField()
    private val colorChooser = JColorChooser()
    private val useDefaultColorCheckbox = JBCheckBox("Use Default Color")
    val preferredFocusedComponent: JComponent
        get() = colorChooser

    var useDefaultColor: Boolean
        get() = useDefaultColorCheckbox.isSelected
        set(value) {
            useDefaultColorCheckbox.isSelected = value
        }
    var chosenColor: Int
        get() = colorChooser.color.rgb
        set(colorRGB) {
            colorChooser.color = Color(colorRGB)
        }
    var logFilePath: String
        get() = logFilePathComponent.text
        set(value) {
            logFilePath
            logFilePathComponent.text = value
        }

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Tabnine Log File Path: ", logFilePathComponent, 1, false)
            .addLabeledComponent(JBLabel("Inline Hint Color:", UIUtil.ComponentStyle.LARGE), colorChooser, 1, true)
            .addComponent(useDefaultColorCheckbox, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}
