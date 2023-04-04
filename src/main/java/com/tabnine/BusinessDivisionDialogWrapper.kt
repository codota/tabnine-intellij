package com.tabnine

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import com.tabnine.userSettings.AppSettingsState.Companion.instance
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class BusinessDivisionDialogWrapper() : DialogWrapper(true) {
    init {
        title = "Business Division"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                label(
                    """
            Please select your business division.
            Without selecting, Tabnine won't start.
            You can also change it from Tabnine settings by clicking the Tabnine icon.
                    """.trimIndent()
                )
            }
            row {
                comboBox<String>(
                    DefaultComboBoxModel(
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
                    ),
                    {
                        instance.businessDivision
                    },
                    {
                        instance.businessDivision = if (!it.isNullOrBlank()) it else ""
                    }
                )
            }
        }
    }
}
