package com.tabnine.dialogs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.ui.Messages

object Dialogs {
    @JvmStatic
    fun showRestartDialog(message: String) {
        ApplicationManager.getApplication().invokeLater {
            val result = Messages.showYesNoDialog(
                message,
                "Tabnine",
                "Restart Now",
                "Restart Later",
                Messages.getInformationIcon()
            )

            if (result == Messages.YES) {
                (ApplicationManager.getApplication() as ApplicationEx).restart(true)
            }
        }
    }
}
