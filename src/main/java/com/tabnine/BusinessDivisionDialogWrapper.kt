package com.tabnine

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.FormBuilder
import com.tabnine.userSettings.AppSettingsState.Companion.instance
import javax.swing.JComponent
import javax.swing.JTextArea

class BusinessDivisionDialogWrapper() : DialogWrapper(true) {

    init {
        title = "Business Division"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val businessDivisionComboBox = ComboBox(
            arrayOf(
                "",
                "Mobile eXperience",
                "Visual Display",
                "Networks",
                "Digital Appliances",
                "Health & Medical Equipment",
                "Samsung Research",
                "Other"
            )
        )
        businessDivisionComboBox.selectedItem = instance.businessDivision

        businessDivisionComboBox.addItemListener {
            instance.businessDivision = businessDivisionComboBox.selectedItem as String
        }

        val label = JTextArea(
            """
            Please select your business division.
            Without selecting, Tabnine won't start.
            You can also change it from Tabnine settings by clicking the Tabnine icon.
            """.trimIndent(),
        )

        val panelBuilder = FormBuilder.createFormBuilder().addLabeledComponent(
            label,
            businessDivisionComboBox, 1, true
        )

        return panelBuilder.panel
    }
}
